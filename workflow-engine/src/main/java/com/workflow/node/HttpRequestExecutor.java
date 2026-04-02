package com.workflow.node;

import org.springframework.stereotype.Component;
import java.net.http.*;
import java.net.URI;
import java.time.Duration;
import java.util.Map;

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
    public NodeExecutionResult execute(NodeExecutionContext context) throws Exception {
        try {
            Map<String, Object> config = context.getNodeConfig();
            
            String method = config != null ? (String) config.get("method") : null;
            String url = config != null ? (String) config.get("url") : null;
            
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
                    String body = config != null ? (String) config.get("body") : null;
                    requestBuilder.POST(body != null ? HttpRequest.BodyPublishers.ofString(body) 
                            : HttpRequest.BodyPublishers.noBody());
                    break;
                case "PUT":
                    String putBody = config != null ? (String) config.get("body") : null;
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
}
