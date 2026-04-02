package com.workflow.node;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 代码节点执行器
 * 支持执行 JavaScript/Java 代码（当前为简化实现）
 */
@Component
public class CodeExecutor implements NodeExecutor {

    @Override
    public String getNodeType() {
        return "code";
    }

    @Override
    public NodeExecutionResult execute(NodeExecutionContext context) throws Exception {
        try {
            Map<String, Object> config = context.getNodeConfig();
            String code = getConfigValue(config, "code", null);

            if (code == null || code.isEmpty()) {
                return NodeExecutionResult.failure("Code is required");
            }

            Object inputData = context.getInputData();
            
            // TODO: 集成 JavaScript 引擎（如 Nashorn 或 GraalVM）来执行实际代码
            // 当前仅为演示实现
            String result = createDemoResponse(code, inputData);

            return NodeExecutionResult.success(result);

        } catch (Exception e) {
            return NodeExecutionResult.failure("Code execution failed: " + e.getMessage());
        }
    }

    /**
     * Create a demo response showing what would be executed
     */
    private String createDemoResponse(String code, Object inputData) {
        return "// Code execution demo\n" +
               "// Input: " + (inputData != null ? inputData.toString() : "null") + "\n" +
               "// Code: " + code + "\n" +
               "// Note: Actual code execution requires JavaScript engine integration";
    }

    /**
     * Safely get a value from config map
     */
    @SuppressWarnings("unchecked")
    private <T> T getConfigValue(Map<String, Object> config, String key, T defaultValue) {
        if (config == null) {
            return defaultValue;
        }
        Object value = config.get(key);
        return value != null ? (T) value : defaultValue;
    }
}
