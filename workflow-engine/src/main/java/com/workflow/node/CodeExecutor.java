package com.workflow.node;

import org.springframework.stereotype.Component;

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
    public NodeExecutionResult execute(String config, String inputData) {
        // In a real implementation, this would execute JavaScript code
        // For security reasons, you'd want to use a sandboxed JS engine
        try {
            // Extract code from config
            String code = extractJsonValue(config, "code");
            
            if (code == null || code.isEmpty()) {
                return NodeExecutionResult.failure("Code is required");
            }
            
            // For now, just return the input data as output
            // A real implementation would use a JS engine like Nashorn or GraalVM
            String result = "// Code execution not fully implemented in this demo\n" +
                          "// Input data: " + (inputData != null ? inputData : "null") + "\n" +
                          "// Code: " + code;
            
            return NodeExecutionResult.success(result);
            
        } catch (Exception e) {
            return NodeExecutionResult.failure("Code execution failed: " + e.getMessage());
        }
    }
    
    private String extractJsonValue(String json, String key) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) {
            return null;
        }
        
        int colonIndex = json.indexOf(':', keyIndex);
        if (colonIndex == -1) {
            return null;
        }
        
        int startIndex = colonIndex + 1;
        while (startIndex < json.length() && Character.isWhitespace(json.charAt(startIndex))) {
            startIndex++;
        }
        
        if (startIndex >= json.length()) {
            return null;
        }
        
        char firstChar = json.charAt(startIndex);
        if (firstChar == '"') {
            // Handle multi-line strings by finding the closing quote
            int endIndex = startIndex + 1;
            while (endIndex < json.length()) {
                if (json.charAt(endIndex) == '"' && json.charAt(endIndex - 1) != '\\') {
                    return json.substring(startIndex + 1, endIndex);
                }
                endIndex++;
            }
            return null;
        } else {
            int endIndex = startIndex;
            while (endIndex < json.length() && json.charAt(endIndex) != ',' && json.charAt(endIndex) != '}') {
                endIndex++;
            }
            return json.substring(startIndex, endIndex).trim();
        }
    }
}
