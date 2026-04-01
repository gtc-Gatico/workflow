package com.workflow.node;

import org.springframework.stereotype.Component;
import java.net.http.*;
import java.net.URI;
import java.time.Duration;

/**
 * Executor for HTTP Request nodes
 */
@Component
public class HttpRequestExecutor implements NodeExecutor {
    
    @Override
    public String getNodeType() {
        return "http_request";
    }
    
    @Override
    public NodeExecutionResult execute(String config, String inputData) {
        try {
            // Parse config (simplified - in production use proper JSON parsing)
            // Config expected format: {"method": "GET", "url": "https://api.example.com", "headers": {...}}
            
            String method = extractJsonValue(config, "method");
            String url = extractJsonValue(config, "url");
            
            if (url == null || url.isEmpty()) {
                return NodeExecutionResult.failure("URL is required");
            }
            
            if (method == null || method.isEmpty()) {
                method = "GET";
            }
            
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
            
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30));
            
            // Set method
            switch (method.toUpperCase()) {
                case "GET":
                    requestBuilder.GET();
                    break;
                case "POST":
                    String body = extractJsonValue(config, "body");
                    requestBuilder.POST(body != null ? HttpRequest.BodyPublishers.ofString(body) 
                            : HttpRequest.BodyPublishers.noBody());
                    break;
                case "PUT":
                    String putBody = extractJsonValue(config, "body");
                    requestBuilder.PUT(putBody != null ? HttpRequest.BodyPublishers.ofString(putBody) 
                            : HttpRequest.BodyPublishers.noBody());
                    break;
                case "DELETE":
                    requestBuilder.DELETE();
                    break;
                default:
                    requestBuilder.GET();
            }
            
            // Set headers
            requestBuilder.header("Content-Type", "application/json");
            requestBuilder.header("User-Agent", "WorkflowEngine/1.0");
            
            HttpResponse<String> response = client.send(
                    requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString()
            );
            
            String responseBody = response.body();
            return NodeExecutionResult.success(responseBody);
            
        } catch (Exception e) {
            return NodeExecutionResult.failure("HTTP request failed: " + e.getMessage());
        }
    }
    
    private String extractJsonValue(String json, String key) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        // Simple extraction - in production use proper JSON library
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
            int endIndex = json.indexOf('"', startIndex + 1);
            if (endIndex == -1) {
                return null;
            }
            return json.substring(startIndex + 1, endIndex);
        } else {
            int endIndex = startIndex;
            while (endIndex < json.length() && json.charAt(endIndex) != ',' && json.charAt(endIndex) != '}') {
                endIndex++;
            }
            return json.substring(startIndex, endIndex).trim();
        }
    }
}
