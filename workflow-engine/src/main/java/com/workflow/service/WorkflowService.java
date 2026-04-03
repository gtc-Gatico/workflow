package com.workflow.service;

import com.workflow.dto.WorkflowDTO;
import com.workflow.dto.WorkflowNodeDTO;
import com.workflow.model.Workflow;
import com.workflow.model.WorkflowExecution;
import com.workflow.model.WorkflowNode;
import com.workflow.node.NodeExecutor;
import com.workflow.node.NodeExecutionResult;
import com.workflow.repository.WorkflowExecutionRepository;
import com.workflow.repository.WorkflowNodeRepository;
import com.workflow.repository.WorkflowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WorkflowService {
    
    @Autowired
    private WorkflowRepository workflowRepository;
    
    @Autowired
    private WorkflowNodeRepository workflowNodeRepository;
    
    @Autowired
    private WorkflowExecutionRepository workflowExecutionRepository;
    
    @Autowired
    private List<NodeExecutor> nodeExecutors;
    
    /**
     * Get all workflows
     */
    public List<Workflow> getAllWorkflows() {
        return workflowRepository.findAll();
    }
    
    /**
     * Get workflow by ID
     */
    public Workflow getWorkflowById(Long id) {
        return workflowRepository.findById(id).orElse(null);
    }
    
    /**
     * Create a new workflow from DTO
     */
    @Transactional
    public Workflow createWorkflowFromDTO(WorkflowDTO workflowDTO) {
        Workflow workflow = new Workflow();
        workflow.setName(workflowDTO.getName());
        workflow.setDescription(workflowDTO.getDescription());
        workflow.setActive(workflowDTO.isActive());
        workflow.setCreatedAt(LocalDateTime.now());
        workflow.setUpdatedAt(LocalDateTime.now());
        
        Workflow savedWorkflow = workflowRepository.save(workflow);
        
        // Save nodes if provided
        if (workflowDTO.getNodes() != null && !workflowDTO.getNodes().isEmpty()) {
            List<WorkflowNode> nodes = convertToEntities(workflowDTO.getNodes(), savedWorkflow);
            workflowNodeRepository.saveAll(nodes);
        }
        
        return savedWorkflow;
    }
    
    /**
     * Update an existing workflow from DTO
     */
    @Transactional
    public Workflow updateWorkflowFromDTO(Long id, WorkflowDTO workflowDTO) {
        Workflow workflow = workflowRepository.findById(id).orElseThrow(
            () -> new RuntimeException("Workflow not found with id: " + id)
        );
        
        workflow.setName(workflowDTO.getName());
        workflow.setDescription(workflowDTO.getDescription());
        workflow.setActive(workflowDTO.isActive());
        workflow.setUpdatedAt(LocalDateTime.now());
        
        Workflow savedWorkflow = workflowRepository.save(workflow);
        
        // Delete existing nodes and save new ones
        workflowNodeRepository.deleteByWorkflowId(id);
        
        if (workflowDTO.getNodes() != null && !workflowDTO.getNodes().isEmpty()) {
            List<WorkflowNode> nodes = convertToEntities(workflowDTO.getNodes(), savedWorkflow);
            workflowNodeRepository.saveAll(nodes);
        }
        
        return savedWorkflow;
    }
    
    /**
     * Convert WorkflowNodeDTO list to WorkflowNode entities
     */
    private List<WorkflowNode> convertToEntities(List<WorkflowNodeDTO> dtoNodes, Workflow workflow) {
        List<WorkflowNode> entities = new ArrayList<>();
        
        for (WorkflowNodeDTO dto : dtoNodes) {
            WorkflowNode entity = new WorkflowNode();
            entity.setWorkflow(workflow);
            entity.setNodeType(dto.getNodeType());
            entity.setType(dto.getType());
            entity.setName(dto.getName());
            entity.setConfig(dto.getConfig());
            entity.setPositionX(dto.getPositionX());
            entity.setPositionY(dto.getPositionY());
            entity.setActive(dto.isActive());
            
            // Parse nextNodeId from string to Long if it's a numeric string
            if (dto.getNextNodeId() != null && !dto.getNextNodeId().isEmpty()) {
                try {
                    // Try to parse as Long (for database IDs)
                    entity.setNextNodeId(Long.parseLong(dto.getNextNodeId()));
                } catch (NumberFormatException e) {
                    // If it's not a numeric string (e.g., "node_1"), we can't store it directly
                    // In this case, we might need to look up the actual node ID or store as null
                    // For now, we'll set it to null and handle resolution during execution
                    entity.setNextNodeId(null);
                }
            }
            
            entities.add(entity);
        }
        
        return entities;
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
            
            // Create a map of node ID to node for quick lookup
            Map<Long, WorkflowNode> nodeMap = nodes.stream()
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
                
                NodeExecutionResult result = executor.execute(currentNode.getConfig(), currentData);
                
                if (!result.isSuccess()) {
                    throw new RuntimeException("Node execution failed: " + result.getError());
                }
                
                currentData = result.getOutputData();
                
                // Move to next node
                Long nextNodeId = result.getNextNodeId() != null ? 
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
}
