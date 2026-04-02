package com.workflow.controller;

import com.workflow.model.Workflow;
import com.workflow.model.WorkflowExecution;
import com.workflow.service.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workflows")
@CrossOrigin(origins = "*")
public class WorkflowController {
    
    @Autowired
    private WorkflowService workflowService;
    
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
     * Create a new workflow
     */
    @PostMapping
    public ResponseEntity<Workflow> createWorkflow(@RequestBody Workflow workflow) {
        Workflow created = workflowService.createWorkflow(workflow);
        return ResponseEntity.ok(created);
    }
    
    /**
     * Update an existing workflow
     */
    @PutMapping("/{id}")
    public ResponseEntity<Workflow> updateWorkflow(
            @PathVariable Long id,
            @RequestBody Workflow workflow) {
        try {
            Workflow updated = workflowService.updateWorkflow(id, workflow);
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
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get executions for a workflow
     */
    @GetMapping("/{id}/executions")
    public ResponseEntity<List<WorkflowExecution>> getWorkflowExecutions(@PathVariable Long id) {
        return ResponseEntity.ok(workflowService.getWorkflowExecutions(id));
    }
    
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
