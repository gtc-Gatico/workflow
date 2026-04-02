package com.workflow.engine.trigger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Webhook 触发器 - 类似 n8n 的 Webhook Trigger
 * 支持自定义路径、HTTP 方法、请求头验证
 */
@RestController
@RequestMapping("/api/webhooks")
public class WebhookTrigger implements WorkflowTrigger {
    
    private static final Logger logger = LoggerFactory.getLogger(WebhookTrigger.class);
    
    // 存储活跃的 webhook 触发器：path -> WebhookTrigger
    private static final ConcurrentHashMap<String, WebhookTrigger> activeWebhooks = new ConcurrentHashMap<>();
    
    private String id;
    private TriggerContext context;
    private String webhookPath;
    private String httpMethod;
    
    public WebhookTrigger() {}
    
    public WebhookTrigger(String id) {
        this.id = id;
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public String getType() {
        return "webhook";
    }
    
    @Override
    public void initialize(TriggerContext context) {
        this.context = context;
        this.webhookPath = context.getConfig().getWebhookPath();
        this.httpMethod = context.getConfig().getHttpMethod();
        
        if (webhookPath == null || webhookPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Webhook path is required");
        }
        
        // 标准化路径
        if (!webhookPath.startsWith("/")) {
            webhookPath = "/" + webhookPath;
        }
        
        logger.info("Initialized webhook trigger: {} {}", 
            httpMethod != null ? httpMethod : "ANY", webhookPath);
    }
    
    @Override
    public void start() {
        String key = getWebhookKey();
        activeWebhooks.put(key, this);
        logger.info("Webhook trigger started: {} (workflow: {})", 
            key, context.getWorkflowId());
    }
    
    @Override
    public void stop() {
        String key = getWebhookKey();
        activeWebhooks.remove(key);
        logger.info("Webhook trigger stopped: {}", key);
    }
    
    @Override
    public void trigger(Map<String, Object> payload) {
        // Webhook 通过 HTTP 请求触发
        logger.debug("Manual trigger not supported for webhook trigger");
    }
    
    private String getWebhookKey() {
        return (httpMethod != null ? httpMethod.toUpperCase() : "ANY") + ":" + webhookPath;
    }
    
    /**
     * 处理 Webhook 请求
     */
    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST, 
        RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public void handleWebhook(@RequestParam Map<String, String> queryParams,
                              @RequestBody(required = false) Map<String, Object> body,
                              @RequestHeader Map<String, String> headers,
                              org.springframework.http.HttpMethod method,
                              jakarta.servlet.http.HttpServletRequest request) {
        
        String path = request.getRequestURI().replace("/api/webhooks", "");
        if (path.isEmpty()) {
            path = "/";
        }
        
        String key = method.name() + ":" + path;
        WebhookTrigger trigger = activeWebhooks.get(key);
        
        // 如果没有匹配到具体方法，尝试查找 ANY 方法
        if (trigger == null) {
            key = "ANY:" + path;
            trigger = activeWebhooks.get(key);
        }
        
        if (trigger == null) {
            logger.warn("No webhook found for: {} {}", method, path);
            return;
        }
        
        try {
            // 应用过滤器
            if (trigger.context.getConfig().getFilters() != null) {
                if (!applyFilters(headers, queryParams, body)) {
                    logger.debug("Webhook request filtered out: {} {}", method, path);
                    return;
                }
            }
            
            // 构建触发数据
            Map<String, Object> data = new HashMap<>();
            data.put("method", method.name());
            data.put("path", path);
            data.put("query", queryParams);
            data.put("body", body != null ? body : new HashMap<>());
            data.put("headers", headers);
            data.put("timestamp", System.currentTimeMillis());
            
            String executionId = java.util.UUID.randomUUID().toString();
            trigger.context.getCallback().onTrigger(executionId, data);
            
            logger.info("Webhook triggered: {} {}, executionId: {}", 
                method, path, executionId);
                
        } catch (Exception e) {
            logger.error("Error processing webhook: {} {}", method, path, e);
        }
    }
    
    private boolean applyFilters(Map<String, String> headers, 
                                  Map<String, String> queryParams,
                                  Map<String, Object> body) {
        // TODO: 实现过滤器逻辑
        return true;
    }
    
    /**
     * 获取所有活跃的 Webhook 路径
     */
    public static Map<String, String> getActiveWebhooks() {
        Map<String, String> result = new HashMap<>();
        activeWebhooks.forEach((key, trigger) -> {
            result.put(key, trigger.context.getWorkflowId());
        });
        return result;
    }
}
