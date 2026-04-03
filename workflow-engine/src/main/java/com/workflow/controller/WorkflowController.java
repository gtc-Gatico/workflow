package com.workflow.controller;

import com.workflow.dto.WorkflowDTO;
import com.workflow.model.Workflow;
import com.workflow.model.WorkflowExecution;
import com.workflow.model.WorkflowNode;
import com.workflow.service.WorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工作流控制器
 * 提供 RESTful API 用于管理工作流和执行记录
 */
@RestController
@RequestMapping("/api/workflows")
@CrossOrigin(origins = "*")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    /**
     * Get all workflows
     */
    @GetMapping
    public ResponseEntity<List<Workflow>> getAllWorkflows() {
        return ResponseEntity.ok(workflowService.getAllWorkflows());
    }

    /**
     * Get workflow by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Workflow> getWorkflowById(@PathVariable Long id) {
        Workflow workflow = workflowService.getWorkflowById(id);
        if (workflow == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(workflow);
    }

    /**
     * Create a new workflow from DTO
     */
    @PostMapping
    public ResponseEntity<Workflow> createWorkflow(@RequestBody WorkflowDTO dto) {
        Workflow workflow = convertToWorkflow(dto);
        Workflow created = workflowService.createWorkflowWithNodes(workflow, dto.getNodes(), dto.getConnections());
        return ResponseEntity.ok(created);
    }

    /**
     * Update an existing workflow
     */
    @PutMapping("/{id}")
    public ResponseEntity<Workflow> updateWorkflow(
            @PathVariable Long id,
            @RequestBody WorkflowDTO dto) {
        try {
            Workflow workflow = convertToWorkflow(dto);
            Workflow updated = workflowService.updateWorkflowWithNodes(id, workflow, dto.getNodes(), dto.getConnections());
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete a workflow
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkflow(@PathVariable Long id) {
        workflowService.deleteWorkflow(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Execute a workflow
     */
    @PostMapping("/{id}/execute")
    public ResponseEntity<WorkflowExecution> executeWorkflow(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> inputData) {
        try {
            String inputJson = inputData != null ? convertToJson(inputData) : "{}";
            WorkflowExecution execution = workflowService.executeWorkflow(id, inputJson);
            return ResponseEntity.ok(execution);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Get executions for a workflow
     */
    @GetMapping("/{id}/executions")
    public ResponseEntity<List<WorkflowExecution>> getWorkflowExecutions(@PathVariable Long id) {
        return ResponseEntity.ok(workflowService.getWorkflowExecutions(id));
    }

    /**
     * Convert WorkflowDTO to Workflow entity
     */
    private Workflow convertToWorkflow(WorkflowDTO dto) {
        Workflow workflow = new Workflow();
        if (dto.getName() != null) {
            workflow.setName(dto.getName());
        } else {
            workflow.setName("Workflow-" + System.currentTimeMillis());
        }
        workflow.setDescription(dto.getDescription());
        workflow.setActive(true);
        return workflow;
    }

    /**
     * Convert Map to JSON string (simple implementation)
     */
    private String convertToJson(Map<String, Object> map) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) json.append(",");
            json.append("\"").append(entry.getKey()).append("\":");
            Object value = entry.getValue();
            if (value instanceof String) {
                json.append("\"").append(value).append("\"");
            } else {
                json.append(value);
            }
            first = false;
        }
        json.append("}");
        return json.toString();
    }
}
