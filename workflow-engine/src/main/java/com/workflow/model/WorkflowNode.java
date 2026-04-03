package com.workflow.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 工作流节点实体类
 * 表示工作流中的单个执行节点，包含节点配置和执行逻辑
 */
@Entity
@Table(name = "workflow_nodes")
public class WorkflowNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    private Workflow workflow;

    @Column(nullable = false)
    private String nodeType; // e.g., "trigger", "action", "condition"

    @Column(nullable = false)
    private String type; // specific type like "webhook", "http_request", "email", etc.

    @Column(nullable = false)
    private String name;

    @Column(length = 5000)
    private String config; // JSON configuration for the node

    @Column(length = 1000)
    private String positionX; // X position in visual editor

    @Column(length = 1000)
    private String positionY; // Y position in visual editor

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "next_node_id")
    private String nextNodeId; // ID of the next node to execute (can be string like "node_1" or numeric ID)

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public WorkflowNode() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Workflow getWorkflow() { return workflow; }
    public void setWorkflow(Workflow workflow) { this.workflow = workflow; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
