package com.workflow.engine.trigger;

import java.time.Instant;
import java.util.Map;

/**
 * 触发器接口 - 类似 n8n 的 Trigger Nodes
 * 支持：定时、Webhook、轮询、事件驱动等
 */
public interface WorkflowTrigger {
    
    /**
     * 触发器唯一标识
     */
    String getId();
    
    /**
     * 触发器类型 (cron, webhook, polling, event)
     */
    String getType();
    
    /**
     * 初始化触发器
     */
    void initialize(TriggerContext context);
    
    /**
     * 启动触发器
     */
    void start();
    
    /**
     * 停止触发器
     */
    void stop();
    
    /**
     * 触发工作流执行
     */
    void trigger(Map<String, Object> payload);
    
    /**
     * 触发器配置
     */
    class TriggerConfig {
        private String cronExpression;      // Cron 表达式 (定时触发)
        private String webhookPath;         // Webhook 路径
        private String httpMethod;          // HTTP 方法
        private Map<String, String> headers;// 请求头
        private Integer pollingInterval;    // 轮询间隔 (秒)
        private String eventType;           // 事件类型
        private Map<String, Object> filters;// 过滤条件
        private Boolean active = true;      // 是否激活
        
        // Getters and Setters
        public String getCronExpression() { return cronExpression; }
        public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }
        public String getWebhookPath() { return webhookPath; }
        public void setWebhookPath(String webhookPath) { this.webhookPath = webhookPath; }
        public String getHttpMethod() { return httpMethod; }
        public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }
        public Map<String, String> getHeaders() { return headers; }
        public void setHeaders(Map<String, String> headers) { this.headers = headers; }
        public Integer getPollingInterval() { return pollingInterval; }
        public void setPollingInterval(Integer pollingInterval) { this.pollingInterval = pollingInterval; }
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        public Map<String, Object> getFilters() { return filters; }
        public void setFilters(Map<String, Object> filters) { this.filters = filters; }
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
    }
    
    /**
     * 触发器上下文
     */
    class TriggerContext {
        private String workflowId;
        private TriggerConfig config;
        private TriggerCallback callback;
        
        public String getWorkflowId() { return workflowId; }
        public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }
        public TriggerConfig getConfig() { return config; }
        public void setConfig(TriggerConfig config) { this.config = config; }
        public TriggerCallback getCallback() { return callback; }
        public void setCallback(TriggerCallback callback) { this.callback = callback; }
    }
    
    /**
     * 触发回调接口
     */
    interface TriggerCallback {
        void onTrigger(String executionId, Map<String, Object> data);
    }
}
