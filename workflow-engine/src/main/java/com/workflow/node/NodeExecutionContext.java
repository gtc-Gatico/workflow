package com.workflow.node;

import java.util.Map;

/**
 * 节点执行上下文 (类似 n8n 的 RunContext)
 * 包含工作流执行过程中的所有状态和数据
 */
public class NodeExecutionContext {
    
    private String workflowId;
    private String executionId;
    private String currentNodeId;
    private Map<String, Object> inputData;
    private Map<String, Object> globalData; // 全局数据，跨节点共享
    private Map<String, Object> credentials; // 凭证数据
    private int retryCount;
    private long timeoutMs;

    public NodeExecutionContext() {
        this.globalData = new java.util.HashMap<>();
        this.credentials = new java.util.HashMap<>();
        this.retryCount = 0;
        this.timeoutMs = 30000L; // 默认 30 秒超时
    }

    // Getters and Setters
    public String getWorkflowId() { return workflowId; }
    public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }

    public String getExecutionId() { return executionId; }
    public void setExecutionId(String executionId) { this.executionId = executionId; }

    public String getCurrentNodeId() { return currentNodeId; }
    public void setCurrentNodeId(String currentNodeId) { this.currentNodeId = currentNodeId; }

    public Map<String, Object> getInputData() { return inputData; }
    public void setInputData(Map<String, Object> inputData) { this.inputData = inputData; }

    public Map<String, Object> getGlobalData() { return globalData; }
    public void setGlobalData(Map<String, Object> globalData) { this.globalData = globalData; }

    public void setGlobalValue(String key, Object value) { 
        if (this.globalData == null) {
            this.globalData = new java.util.HashMap<>();
        }
        this.globalData.put(key, value); 
    }

    public Object getGlobalValue(String key) {
        return this.globalData != null ? this.globalData.get(key) : null;
    }

    public Map<String, Object> getCredentials() { return credentials; }
    public void setCredentials(Map<String, Object> credentials) { this.credentials = credentials; }

    public void setCredential(String name, Object credential) {
        if (this.credentials == null) {
            this.credentials = new java.util.HashMap<>();
        }
        this.credentials.put(name, credential);
    }

    public Object getCredential(String name) {
        return this.credentials != null ? this.credentials.get(name) : null;
    }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    public long getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(long timeoutMs) { this.timeoutMs = timeoutMs; }
}
