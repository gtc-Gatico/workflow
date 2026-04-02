package com.workflow.engine.version;

import java.time.Instant;
import java.util.List;

/**
 * 工作流版本控制 - 类似 n8n 的版本管理
 * 支持：多版本、草稿、发布、回滚
 */
public class WorkflowVersion {
    
    private String id;
    private String workflowId;
    private Integer versionNumber;
    private String name;
    private String description;
    private WorkflowDefinition definition;
    private VersionStatus status;
    private Instant createdAt;
    private Instant publishedAt;
    private String createdBy;
    private String publishedBy;
    private List<String> tags;
    private Boolean isCurrent;
    
    public enum VersionStatus {
        DRAFT,      // 草稿
        PUBLISHED,  // 已发布
        ARCHIVED,   // 已归档
        DEPRECATED  // 已废弃
    }
    
    /**
     * 工作流定义
     */
    public static class WorkflowDefinition {
        private List<Node> nodes;
        private List<Connection> connections;
        private Map<String, Object> settings;
        private List<TriggerConfig> triggers;
        
        // Getters and Setters
        public List<Node> getNodes() { return nodes; }
        public void setNodes(List<Node> nodes) { this.nodes = nodes; }
        public List<Connection> getConnections() { return connections; }
        public void setConnections(List<Connection> connections) { this.connections = connections; }
        public Map<String, Object> getSettings() { return settings; }
        public void setSettings(Map<String, Object> settings) { this.settings = settings; }
        public List<TriggerConfig> getTriggers() { return triggers; }
        public void setTriggers(List<TriggerConfig> triggers) { this.triggers = triggers; }
    }
    
    /**
     * 节点定义
     */
    public static class Node {
        private String id;
        private String name;
        private String type;
        private Map<String, Object> parameters;
        private Position position;
        private List<String> inputTypes;
        private List<String> outputTypes;
        private Boolean disabled;
        private RetryConfig retry;
        private TimeoutConfig timeout;
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Map<String, Object> getParameters() { return parameters; }
        public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
        public Position getPosition() { return position; }
        public void setPosition(Position position) { this.position = position; }
        public List<String> getInputTypes() { return inputTypes; }
        public void setInputTypes(List<String> inputTypes) { this.inputTypes = inputTypes; }
        public List<String> getOutputTypes() { return outputTypes; }
        public void setOutputTypes(List<String> outputTypes) { this.outputTypes = outputTypes; }
        public Boolean getDisabled() { return disabled; }
        public void setDisabled(Boolean disabled) { this.disabled = disabled; }
        public RetryConfig getRetry() { return retry; }
        public void setRetry(RetryConfig retry) { this.retry = retry; }
        public TimeoutConfig getTimeout() { return timeout; }
        public void setTimeout(TimeoutConfig timeout) { this.timeout = timeout; }
    }
    
    /**
     * 连接定义
     */
    public static class Connection {
        private String id;
        private String sourceNodeId;
        private String sourceOutput;
        private String targetNodeId;
        private String targetInput;
        private Map<String, Object> conditions;
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getSourceNodeId() { return sourceNodeId; }
        public void setSourceNodeId(String sourceNodeId) { this.sourceNodeId = sourceNodeId; }
        public String getSourceOutput() { return sourceOutput; }
        public void setSourceOutput(String sourceOutput) { this.sourceOutput = sourceOutput; }
        public String getTargetNodeId() { return targetNodeId; }
        public void setTargetNodeId(String targetNodeId) { this.targetNodeId = targetNodeId; }
        public String getTargetInput() { return targetInput; }
        public void setTargetInput(String targetInput) { this.targetInput = targetInput; }
        public Map<String, Object> getConditions() { return conditions; }
        public void setConditions(Map<String, Object> conditions) { this.conditions = conditions; }
    }
    
    /**
     * 位置信息
     */
    public static class Position {
        private Double x;
        private Double y;
        
        public Double getX() { return x; }
        public void setX(Double x) { this.x = x; }
        public Double getY() { return y; }
        public void setY(Double y) { this.y = y; }
    }
    
    /**
     * 重试配置
     */
    public static class RetryConfig {
        private Integer maxAttempts;
        private Integer delayBetweenAttempts;
        private List<Integer> retryOn;
        
        public Integer getMaxAttempts() { return maxAttempts; }
        public void setMaxAttempts(Integer maxAttempts) { this.maxAttempts = maxAttempts; }
        public Integer getDelayBetweenAttempts() { return delayBetweenAttempts; }
        public void setDelayBetweenAttempts(Integer delayBetweenAttempts) { 
            this.delayBetweenAttempts = delayBetweenAttempts; 
        }
        public List<Integer> getRetryOn() { return retryOn; }
        public void setRetryOn(List<Integer> retryOn) { this.retryOn = retryOn; }
    }
    
    /**
     * 超时配置
     */
    public static class TimeoutConfig {
        private Integer milliseconds;
        private String behavior; // "cancel" or "continue"
        
        public Integer getMilliseconds() { return milliseconds; }
        public void setMilliseconds(Integer milliseconds) { this.milliseconds = milliseconds; }
        public String getBehavior() { return behavior; }
        public void setBehavior(String behavior) { this.behavior = behavior; }
    }
    
    /**
     * 触发器配置
     */
    public static class TriggerConfig {
        private String type;
        private Map<String, Object> properties;
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Map<String, Object> getProperties() { return properties; }
        public void setProperties(Map<String, Object> properties) { this.properties = properties; }
    }
    
    // Getters and Setters for WorkflowVersion
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getWorkflowId() { return workflowId; }
    public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }
    public Integer getVersionNumber() { return versionNumber; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public WorkflowDefinition getDefinition() { return definition; }
    public void setDefinition(WorkflowDefinition definition) { this.definition = definition; }
    public VersionStatus getStatus() { return status; }
    public void setStatus(VersionStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getPublishedBy() { return publishedBy; }
    public void setPublishedBy(String publishedBy) { this.publishedBy = publishedBy; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public Boolean getIsCurrent() { return isCurrent; }
    public void setIsCurrent(Boolean isCurrent) { this.isCurrent = isCurrent; }
}
