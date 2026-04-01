package com.workflow.node;

import org.springframework.stereotype.Component;

/**
 * Executor for Webhook trigger nodes
 */
@Component
public class WebhookExecutor implements NodeExecutor {
    
    @Override
    public String getNodeType() {
        return "webhook";
    }
    
    @Override
    public NodeExecutionResult execute(String config, String inputData) {
        // Webhook nodes are triggers, they pass through the input data
        // In a real implementation, this would set up a webhook endpoint
        try {
            // For now, just pass through the input data
            if (inputData == null || inputData.isEmpty()) {
                return NodeExecutionResult.success("{}");
            }
            return NodeExecutionResult.success(inputData);
        } catch (Exception e) {
            return NodeExecutionResult.failure("Webhook execution failed: " + e.getMessage());
        }
    }
}
