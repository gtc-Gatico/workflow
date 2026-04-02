package com.workflow.node;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * 节点执行结果 (类似 n8n 的 IRunData)
 */
public class NodeExecutionResult {
    
    private boolean success;
    private String outputData;
    private Map<String, Object> outputObject; // 支持结构化输出
    private List<Map<String, Object>> items; // 支持多项输出 (类似 n8n 的 items)
    private String error;
    private Long nextNodeId;
    private List<String> branchOutputs; // 多分支输出
    private long executionTimeMs;
    private Map<String, Object> metadata;

    public NodeExecutionResult() {
        this.items = new ArrayList<>();
        this.branchOutputs = new ArrayList<>();
        this.metadata = new HashMap<>();
    }
    
    public NodeExecutionResult(boolean success, String outputData, String error, Long nextNodeId) {
        this();
        this.success = success;
        this.outputData = outputData;
        this.error = error;
        this.nextNodeId = nextNodeId;
    }
    
    public static NodeExecutionResult success(String outputData) {
        return new NodeExecutionResult(true, outputData, null, null);
    }
    
    public static NodeExecutionResult success(Map<String, Object> outputObject) {
        NodeExecutionResult result = new NodeExecutionResult();
        result.setSuccess(true);
        result.setOutputObject(outputObject);
        return result;
    }

    public static NodeExecutionResult success(List<Map<String, Object>> items) {
        NodeExecutionResult result = new NodeExecutionResult();
        result.setSuccess(true);
        result.setItems(items);
        return result;
    }
    
    public static NodeExecutionResult success(String outputData, Long nextNodeId) {
        return new NodeExecutionResult(true, outputData, null, nextNodeId);
    }

    public static NodeExecutionResult success(Map<String, Object> outputObject, Long nextNodeId) {
        NodeExecutionResult result = success(outputObject);
        result.setNextNodeId(nextNodeId);
        return result;
    }
    
    public static NodeExecutionResult failure(String error) {
        return new NodeExecutionResult(false, null, error, null);
    }

    public static NodeExecutionResult failure(String error, long executionTimeMs) {
        NodeExecutionResult result = new NodeExecutionResult(false, null, error, null);
        result.setExecutionTimeMs(executionTimeMs);
        return result;
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getOutputData() { return outputData; }
    public void setOutputData(String outputData) { this.outputData = outputData; }
    
    public Map<String, Object> getOutputObject() { return outputObject; }
    public void setOutputObject(Map<String, Object> outputObject) { this.outputObject = outputObject; }

    public List<Map<String, Object>> getItems() { return items; }
    public void setItems(List<Map<String, Object>> items) { this.items = items; }
    
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    
    public Long getNextNodeId() { return nextNodeId; }
    public void setNextNodeId(Long nextNodeId) { this.nextNodeId = nextNodeId; }

    public List<String> getBranchOutputs() { return branchOutputs; }
    public void setBranchOutputs(List<String> branchOutputs) { this.branchOutputs = branchOutputs; }

    public long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }
}
