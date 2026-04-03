package com.workflow.dto;

import java.util.List;
import java.util.Map;

/**
 * 工作流数据传输对象
 * 用于接收前端发送的工作流数据（包含 nodes 和 connections）
 */
public class WorkflowDTO {
    
    private List<NodeDTO> nodes;
    private List<ConnectionDTO> connections;
    private String name;
    private String description;
    private String savedAt;

    public static class NodeDTO {
        private String id;
        private String type;
        private String nodeType;
        private String name;
        private Map<String, Object> config;
        private String positionX;
        private String positionY;
        private boolean active;
        private String nextNodeId;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getNodeType() { return nodeType; }
        public void setNodeType(String nodeType) { this.nodeType = nodeType; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Map<String, Object> getConfig() { return config; }
        public void setConfig(Map<String, Object> config) { this.config = config; }
        
        public String getPositionX() { return positionX; }
        public void setPositionX(String positionX) { this.positionX = positionX; }
        
        public String getPositionY() { return positionY; }
        public void setPositionY(String positionY) { this.positionY = positionY; }
        
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        
        public String getNextNodeId() { return nextNodeId; }
        public void setNextNodeId(String nextNodeId) { this.nextNodeId = nextNodeId; }
    }

    public static class ConnectionDTO {
        private String from;
        private String to;

        // Getters and Setters
        public String getFrom() { return from; }
        public void setFrom(String from) { this.from = from; }
        
        public String getTo() { return to; }
        public void setTo(String to) { this.to = to; }
    }

    // Getters and Setters
    public List<NodeDTO> getNodes() { return nodes; }
    public void setNodes(List<NodeDTO> nodes) { this.nodes = nodes; }
    
    public List<ConnectionDTO> getConnections() { return connections; }
    public void setConnections(List<ConnectionDTO> connections) { this.connections = connections; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getSavedAt() { return savedAt; }
    public void setSavedAt(String savedAt) { this.savedAt = savedAt; }
}
