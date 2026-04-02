package com.workflow.node;

import java.util.Map;
import java.util.List;

/**
 * 节点执行器接口 (类似 n8n 的 INodeType)
 * 所有节点类型必须实现此接口
 */
public interface NodeExecutor {

    /**
     * 获取节点类型名称
     */
    String getNodeType();

    /**
     * 获取节点描述信息
     */
    default String getDescription() {
        return "Base node executor";
    }

    /**
     * 执行节点逻辑
     * @param context 执行上下文
     * @return 执行结果
     */
    NodeExecutionResult execute(NodeExecutionContext context) throws Exception;

    /**
     * 兼容旧接口的执行方法
     */
    default NodeExecutionResult execute(String config, String inputData) {
        try {
            NodeExecutionContext context = new NodeExecutionContext();
            context.setInputData(parseJson(inputData));
            // 这里需要解析 config，简化处理
            return execute(context);
        } catch (Exception e) {
            return NodeExecutionResult.error(e.getMessage());
        }
    }

    private Map<String, Object> parseJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new java.util.HashMap<>();
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(json, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON: " + e.getMessage(), e);
        }
    }

    /**
     * 验证节点配置是否有效
     */
    default boolean validate(Map<String, Object> properties) {
        return true;
    }

    /**
     * 获取节点支持的输入数据类型
     */
    default List<String> getInputTypes() {
        return List.of("*");
    }

    /**
     * 获取节点支持的输出数据类型
     */
    default List<String> getOutputTypes() {
        return List.of("*");
    }
}
