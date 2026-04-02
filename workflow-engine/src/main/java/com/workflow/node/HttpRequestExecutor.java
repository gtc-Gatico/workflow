package com.workflow.node;

import org.springframework.stereotype.Component;

import java.net.http.*;
import java.net.URI;
import java.time.Duration;
import java.util.Map;

/**
 * HTTP 请求节点执行器
 * 支持 GET、POST、PUT、DELETE 等 HTTP 方法
 */
@Component
public class HttpRequestExecutor implements NodeExecutor {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    @Override
    public String getNodeType() {
        return "http_request";
    }

    @Override
    public NodeExecutionResult execute(NodeExecutionContext context) throws Exception {
        try {
            Map<String, Object> config = context.getNodeConfig();

            String method = getConfigValue(config, "method", "GET");
            String url = getConfigValue(config, "url", null);

            if (url == null || url.isEmpty()) {
                return NodeExecutionResult.failure("URL is required");
            }

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(CONNECT_TIMEOUT)
                    .build();

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(REQUEST_TIMEOUT);

            // Set method and body
            setRequestMethod(requestBuilder, method, config);

            // Set headers
            requestBuilder.header("Content-Type", "application/json");
            requestBuilder.header("User-Agent", "WorkflowEngine/1.0");

            HttpResponse<String> response = client.send(
                    requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            return NodeExecutionResult.success(response.body());

        } catch (Exception e) {
            return NodeExecutionResult.failure("HTTP request failed: " + e.getMessage());
        }
    }

    /**
     * Set HTTP method and body for the request
     */
    private void setRequestMethod(HttpRequest.Builder builder, String method, Map<String, Object> config) {
        switch (method.toUpperCase()) {
            case "GET":
                builder.GET();
                break;
            case "POST":
                String postBody = getConfigValue(config, "body", null);
                builder.POST(postBody != null ? HttpRequest.BodyPublishers.ofString(postBody)
                        : HttpRequest.BodyPublishers.noBody());
                break;
            case "PUT":
                String putBody = getConfigValue(config, "body", null);
                builder.PUT(putBody != null ? HttpRequest.BodyPublishers.ofString(putBody)
                        : HttpRequest.BodyPublishers.noBody());
                break;
            case "DELETE":
                builder.DELETE();
                break;
            default:
                builder.GET();
        }
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
