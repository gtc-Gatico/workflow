package com.workflow.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "workflow_executions")
public class WorkflowExecution {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    private Workflow workflow;
    
    @Column(nullable = false)
    private String status; // PENDING, RUNNING, SUCCESS, FAILED
    
    @Column(length = 5000)
    private String inputData; // JSON input data
    
    @Column(length = 5000)
    private String outputData; // JSON output data
    
    @Column(length = 10000)
    private String error; // Error message if failed
    
    @Column(nullable = false)
    private LocalDateTime startedAt;
    
    @Column
    private LocalDateTime completedAt;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Workflow getWorkflow() { return workflow; }
    public void setWorkflow(Workflow workflow) { this.workflow = workflow; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getInputData() { return inputData; }
    public void setInputData(String inputData) { this.inputData = inputData; }
    
    public String getOutputData() { return outputData; }
    public void setOutputData(String outputData) { this.outputData = outputData; }
    
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
