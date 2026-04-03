package com.workflow.dto;

import java.util.List;

/**
 * Data Transfer Object for Workflow
 * Handles frontend data format with string-based node IDs
 */
public class WorkflowDTO {
    
    private Long id;
    private String name;
    private String description;
    private boolean active = true;
    private List<WorkflowNodeDTO> nodes;
    
    // Constructors
    public WorkflowDTO() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public List<WorkflowNodeDTO> getNodes() { return nodes; }
    public void setNodes(List<WorkflowNodeDTO> nodes) { this.nodes = nodes; }
}
