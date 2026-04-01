package com.workflow.node;

/**
 * Result of a node execution
 */
public class NodeExecutionResult {
    
    private boolean success;
    private String outputData;
    private String error;
    private Long nextNodeId;
    
    public NodeExecutionResult() {}
    
    public NodeExecutionResult(boolean success, String outputData, String error, Long nextNodeId) {
        this.success = success;
        this.outputData = outputData;
        this.error = error;
        this.nextNodeId = nextNodeId;
    }
    
    public static NodeExecutionResult success(String outputData) {
        return new NodeExecutionResult(true, outputData, null, null);
    }
    
    public static NodeExecutionResult success(String outputData, Long nextNodeId) {
        return new NodeExecutionResult(true, outputData, null, nextNodeId);
    }
    
    public static NodeExecutionResult failure(String error) {
        return new NodeExecutionResult(false, null, error, null);
    }
    
    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getOutputData() { return outputData; }
    public void setOutputData(String outputData) { this.outputData = outputData; }
    
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    
    public Long getNextNodeId() { return nextNodeId; }
    public void setNextNodeId(Long nextNodeId) { this.nextNodeId = nextNodeId; }
}
