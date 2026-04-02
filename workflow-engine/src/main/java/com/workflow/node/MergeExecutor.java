package com.workflow.node;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Merge 节点执行器 (类似 n8n 的 Merge Node)
 * 合并多个输入分支的数据
 */
public class MergeExecutor implements NodeExecutor {

    @Override
    public String getNodeType() {
        return "merge";
    }

    @Override
    public String getDescription() {
        return "Merge data from multiple branches";
    }

    @Override
    public NodeExecutionResult execute(NodeExecutionContext context) throws Exception {
        Map<String, Object> inputData = context.getInputData();
        if (inputData == null) {
            return NodeExecutionResult.failure("No input data provided");
        }

        // 从上下文中获取节点配置
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) context.getGlobalValue("nodeProperties");
        
        if (properties == null) {
            return NodeExecutionResult.success(inputData);
        }

        String mode = (String) properties.getOrDefault("mode", "append");
        
        switch (mode) {
            case "append":
                return executeAppend(inputData, properties);
            case "mergeByPosition":
                return executeMergeByPosition(inputData, properties);
            case "mergeByKey":
                return executeMergeByKey(inputData, properties);
            case "wait":
                return executeWait(inputData, properties);
            default:
                return executeAppend(inputData, properties);
        }
    }

    /**
     * Append 模式：简单追加所有输入
     */
    private NodeExecutionResult executeAppend(Map<String, Object> inputData, Map<String, Object> properties) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 如果输入是列表，直接添加
        if (inputData.containsKey("items") && inputData.get("items") instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) inputData.get("items");
            result.addAll(items);
        } else {
            result.add(inputData);
        }

        return NodeExecutionResult.success(result);
    }

    /**
     * Merge By Position 模式：按位置合并
     */
    private NodeExecutionResult executeMergeByPosition(Map<String, Object> inputData, Map<String, Object> properties) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> inputs = (List<Map<String, Object>>) inputData.get("inputs");
        
        if (inputs == null || inputs.isEmpty()) {
            return NodeExecutionResult.success(inputData);
        }

        int maxItems = inputs.stream().mapToInt(i -> {
            if (i instanceof List) {
                return ((List<?>) i).size();
            }
            return 1;
        }).max().orElse(0);

        for (int i = 0; i < maxItems; i++) {
            Map<String, Object> merged = new HashMap<>();
            for (Map<String, Object> input : inputs) {
                if (input instanceof List && i < ((List<?>) input).size()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> item = (Map<String, Object>) ((List<?>) input).get(i);
                    merged.putAll(item);
                } else if (!(input instanceof List) && i == 0) {
                    merged.putAll(input);
                }
            }
            result.add(merged);
        }

        return NodeExecutionResult.success(result);
    }

    /**
     * Merge By Key 模式：按指定键合并
     */
    private NodeExecutionResult executeMergeByKey(Map<String, Object> inputData, Map<String, Object> properties) {
        String keyName = (String) properties.getOrDefault("keyName", "id");
        Map<Object, Map<String, Object>> resultMap = new HashMap<>();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> inputs = (List<Map<String, Object>>) inputData.get("inputs");
        
        if (inputs == null || inputs.isEmpty()) {
            return NodeExecutionResult.success(inputData);
        }

        for (Map<String, Object> input : inputs) {
            if (input instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> items = (List<Map<String, Object>>) input;
                for (Map<String, Object> item : items) {
                    Object keyValue = item.get(keyName);
                    if (keyValue != null) {
                        resultMap.computeIfAbsent(keyValue, k -> new HashMap<>()).putAll(item);
                    }
                }
            } else {
                Object keyValue = input.get(keyName);
                if (keyValue != null) {
                    resultMap.computeIfAbsent(keyValue, k -> new HashMap<>()).putAll(input);
                }
            }
        }

        return NodeExecutionResult.success(new ArrayList<>(resultMap.values()));
    }

    /**
     * Wait 模式：等待所有输入完成后再处理
     */
    private NodeExecutionResult executeWait(Map<String, Object> inputData, Map<String, Object> properties) {
        // 实际场景中需要等待所有上游分支完成
        // 这里简化处理，直接返回所有输入
        return executeAppend(inputData, properties);
    }

    @Override
    public List<String> getInputTypes() {
        return List.of("*");
    }

    @Override
    public List<String> getOutputTypes() {
        return List.of("*");
    }
}
