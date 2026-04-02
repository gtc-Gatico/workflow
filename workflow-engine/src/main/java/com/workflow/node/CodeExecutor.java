package com.workflow.node;

import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * Executor for Code/JavaScript nodes
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
            String code = config != null ? (String) config.get("code") : null;

            if (code == null || code.isEmpty()) {
                return NodeExecutionResult.failure("Code is required");
            }

            Object inputData = context.getInputData();
            String result = "// Code execution demo\n" +
                          "// Input: " + (inputData != null ? inputData.toString() : "null") + "\n" +
                          "// Code: " + code;

            return NodeExecutionResult.success(result);

        } catch (Exception e) {
            return NodeExecutionResult.failure("Code execution failed: " + e.getMessage());
        }
    }
}
