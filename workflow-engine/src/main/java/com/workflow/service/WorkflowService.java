package com.workflow.service;

import com.workflow.model.Workflow;
import com.workflow.model.WorkflowExecution;
import com.workflow.model.WorkflowNode;
import com.workflow.node.NodeExecutor;
import com.workflow.node.NodeExecutionResult;
import com.workflow.node.NodeExecutionContext;
import com.workflow.repository.WorkflowExecutionRepository;
import com.workflow.repository.WorkflowNodeRepository;
import com.workflow.repository.WorkflowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工作流服务类
 * 提供工作流的 CRUD 操作和执行功能
 */
@Service
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowNodeRepository workflowNodeRepository;
    private final WorkflowExecutionRepository workflowExecutionRepository;
    private final List<NodeExecutor> nodeExecutors;

    @Autowired
    public WorkflowService(WorkflowRepository workflowRepository,
                          WorkflowNodeRepository workflowNodeRepository,
                          WorkflowExecutionRepository workflowExecutionRepository,
                          List<NodeExecutor> nodeExecutors) {
        this.workflowRepository = workflowRepository;
        this.workflowNodeRepository = workflowNodeRepository;
        this.workflowExecutionRepository = workflowExecutionRepository;
        this.nodeExecutors = nodeExecutors;
    }

    /**
     * Get all workflows
     */
    public List<Workflow> getAllWorkflows() {
        return workflowRepository.findAll();
    }

    /**
     * Get workflow by ID with nodes
     */
    public Workflow getWorkflowById(Long id) {
        return workflowRepository.findById(id).orElse(null);
    }

    /**
     * Create a new workflow
     */
    @Transactional
    public Workflow createWorkflow(Workflow workflow) {
        workflow.setCreatedAt(LocalDateTime.now());
        workflow.setUpdatedAt(LocalDateTime.now());
        return workflowRepository.save(workflow);
    }

    /**
     * Update an existing workflow
     */
    @Transactional
    public Workflow updateWorkflow(Long id, Workflow workflowDetails) {
        Workflow workflow = workflowRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Workflow not found with id: " + id));

        workflow.setName(workflowDetails.getName());
        workflow.setDescription(workflowDetails.getDescription());
        workflow.setActive(workflowDetails.isActive());
        workflow.setUpdatedAt(LocalDateTime.now());

        return workflowRepository.save(workflow);
    }

    /**
     * Delete a workflow
     */
    @Transactional
    public void deleteWorkflow(Long id) {
        workflowRepository.deleteById(id);
    }

    /**
     * Execute a workflow
     */
    @Transactional
    public WorkflowExecution executeWorkflow(Long workflowId, String inputData) {
        Workflow workflow = workflowRepository.findById(workflowId)
            .orElseThrow(() -> new RuntimeException("Workflow not found"));

        if (!workflow.isActive()) {
            throw new RuntimeException("Workflow is not active");
        }

        // Create execution record
        WorkflowExecution execution = new WorkflowExecution();
        execution.setWorkflow(workflow);
        execution.setStatus("RUNNING");
        execution.setInputData(inputData);
        execution.setStartedAt(LocalDateTime.now());
        execution = workflowExecutionRepository.save(execution);

        try {
            // Get all nodes for this workflow
            List<WorkflowNode> nodes = workflowNodeRepository.findByWorkflowIdAndActive(workflowId, true);

            if (nodes.isEmpty()) {
                throw new RuntimeException("No active nodes found in workflow");
            }

            // Create a map of node ID (string) to node for quick lookup
            // Node IDs are now strings directly from the database
            Map<String, WorkflowNode> nodeMap = nodes.stream()
                .collect(Collectors.toMap(WorkflowNode::getId, node -> node));

            // Find the first node (trigger node)
            WorkflowNode currentNode = nodes.stream()
                .filter(n -> "trigger".equals(n.getNodeType()))
                .findFirst()
                .orElse(nodes.get(0)); // Fallback to first node

            String currentData = inputData;

            // Execute nodes in sequence
            while (currentNode != null) {
                NodeExecutor executor = findExecutor(currentNode.getType());

                if (executor == null) {
                    throw new RuntimeException("No executor found for node type: " + currentNode.getType());
                }

                // Build execution context
                NodeExecutionContext context = buildExecutionContext(execution, currentNode, currentData);

                NodeExecutionResult result;
                try {
                    result = executor.execute(context);
                } catch (Exception e) {
                    throw new RuntimeException("Node execution failed: " + e.getMessage(), e);
                }

                if (!result.isSuccess()) {
                    throw new RuntimeException("Node execution failed: " + result.getError());
                }

                currentData = result.getOutputData() != null ? result.getOutputData() : currentData;

                // Move to next node
                String nextNodeId = result.getNextNodeId() != null ?
                    result.getNextNodeId() : currentNode.getNextNodeId();

                if (nextNodeId != null && nodeMap.containsKey(nextNodeId)) {
                    currentNode = nodeMap.get(nextNodeId);
                } else {
                    currentNode = null;
                }
            }

            // Update execution record
            execution.setStatus("SUCCESS");
            execution.setOutputData(currentData);
            execution.setCompletedAt(LocalDateTime.now());

        } catch (Exception e) {
            execution.setStatus("FAILED");
            execution.setError(e.getMessage());
            execution.setCompletedAt(LocalDateTime.now());
            throw e;
        } finally {
            workflowExecutionRepository.save(execution);
        }

        return execution;
    }

    /**
     * Build execution context for a node
     */
    private NodeExecutionContext buildExecutionContext(WorkflowExecution execution,
                                                       WorkflowNode node,
                                                       String inputData) {
        NodeExecutionContext context = new NodeExecutionContext();
        context.setWorkflowId(execution.getWorkflow().getId().toString());
        context.setExecutionId(execution.getId().toString());
        context.setCurrentNodeId(node.getId());
        context.setInputData(parseJson(inputData));
        context.setNodeConfig(parseJson(node.getConfig()));
        return context;
    }

    /**
     * Get executions for a workflow
     */
    public List<WorkflowExecution> getWorkflowExecutions(Long workflowId) {
        return workflowExecutionRepository.findByWorkflowId(workflowId);
    }

    /**
     * Find the appropriate executor for a node type
     */
    private NodeExecutor findExecutor(String nodeType) {
        return nodeExecutors.stream()
            .filter(executor -> executor.getNodeType().equals(nodeType))
            .findFirst()
            .orElse(null);
    }

    /**
     * Parse JSON string to Map
     */
    private Map<String, Object> parseJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new java.util.HashMap<>();
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(json, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON: " + e.getMessage(), e);
        }
    }
}
