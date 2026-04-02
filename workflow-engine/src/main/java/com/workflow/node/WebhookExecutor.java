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
    public NodeExecutionResult execute(NodeExecutionContext context) throws Exception {
        // Webhook nodes are triggers, they pass through the input data
        // In a real implementation, this would set up a webhook endpoint
        try {
            Object inputData = context.getInputData();
            if (inputData == null) {
                return NodeExecutionResult.success("{}");
            }
            if (inputData instanceof String) {
                return NodeExecutionResult.success((String) inputData);
            }
            return NodeExecutionResult.success(inputData.toString());
        } catch (Exception e) {
            return NodeExecutionResult.failure("Webhook execution failed: " + e.getMessage());
        }
    }
}
