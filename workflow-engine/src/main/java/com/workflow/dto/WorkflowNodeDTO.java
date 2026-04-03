package com.workflow.dto;

/**
 * Data Transfer Object for WorkflowNode
 * Handles string-based IDs from frontend while backend uses Long
 */
public class WorkflowNodeDTO {
    
    private String id;
    private Long workflowId;
    private String nodeType;
    private String type;
    private String name;
    private String config;
    private String positionX;
    private String positionY;
    private boolean active = true;
    private String nextNodeId;
    
    // Constructors
    public WorkflowNodeDTO() {}
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public Long getWorkflowId() { return workflowId; }
    public void setWorkflowId(Long workflowId) { this.workflowId = workflowId; }
    
    public String getNodeType() { return nodeType; }
    public void setNodeType(String nodeType) { this.nodeType = nodeType; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getConfig() { return config; }
    public void setConfig(String config) { this.config = config; }
    
    public String getPositionX() { return positionX; }
    public void setPositionX(String positionX) { this.positionX = positionX; }
    
    public String getPositionY() { return positionY; }
    public void setPositionY(String positionY) { this.positionY = positionY; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public String getNextNodeId() { return nextNodeId; }
    public void setNextNodeId(String nextNodeId) { this.nextNodeId = nextNodeId; }
}
