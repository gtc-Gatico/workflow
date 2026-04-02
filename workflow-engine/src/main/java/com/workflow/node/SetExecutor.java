package com.workflow.node;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Set 节点执行器 (类似 n8n 的 Set/Edit Fields Node)
 * 设置、修改或删除数据字段
 */
public class SetExecutor implements NodeExecutor {

    @Override
    public String getNodeType() {
        return "set";
    }

    @Override
    public String getDescription() {
        return "Set, modify or remove fields in data";
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

        String mode = (String) properties.getOrDefault("mode", "assign"); // assign, merge, remove
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> assignments = (List<Map<String, Object>>) properties.get("assignments");
        
        Map<String, Object> result;
        
        if ("remove".equals(mode)) {
            result = new HashMap<>(inputData);
            if (assignments != null) {
                for (Map<String, Object> assignment : assignments) {
                    String key = (String) assignment.get("key");
                    if (key != null) {
                        removeNestedValue(result, key);
                    }
                }
            }
        } else if ("merge".equals(mode)) {
            result = new HashMap<>(inputData);
            if (assignments != null) {
                for (Map<String, Object> assignment : assignments) {
                    String key = (String) assignment.get("key");
                    Object value = assignment.get("value");
                    if (key != null) {
                        setNestedValue(result, key, value);
                    }
                }
            }
        } else {
            // assign 模式：只保留设置的字段
            result = new HashMap<>();
            if (assignments != null) {
                for (Map<String, Object> assignment : assignments) {
                    String key = (String) assignment.get("key");
                    Object value = assignment.get("value");
                    if (key != null) {
                        setNestedValue(result, key, value);
                    }
                }
            }
        }

        // 处理多项输入
        List<Map<String, Object>> outputItems = new ArrayList<>();
        if (inputData.containsKey("items") && inputData.get("items") instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> allItems = (List<Map<String, Object>>) inputData.get("items");
            
            for (Map<String, Object> item : allItems) {
                outputItems.add(processItem(item, properties));
            }
        } else {
            outputItems.add(result);
        }

        return NodeExecutionResult.success(outputItems);
    }

    private Map<String, Object> processItem(Map<String, Object> item, Map<String, Object> properties) {
        String mode = (String) properties.getOrDefault("mode", "assign");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> assignments = (List<Map<String, Object>>) properties.get("assignments");
        
        Map<String, Object> result;
        
        if ("remove".equals(mode)) {
            result = new HashMap<>(item);
            if (assignments != null) {
                for (Map<String, Object> assignment : assignments) {
                    String key = (String) assignment.get("key");
                    if (key != null) {
                        removeNestedValue(result, key);
                    }
                }
            }
        } else if ("merge".equals(mode)) {
            result = new HashMap<>(item);
            if (assignments != null) {
                for (Map<String, Object> assignment : assignments) {
                    String key = (String) assignment.get("key");
                    Object value = assignment.get("value");
                    if (key != null) {
                        setNestedValue(result, key, value);
                    }
                }
            }
        } else {
            // assign 模式
            result = new HashMap<>();
            if (assignments != null) {
                for (Map<String, Object> assignment : assignments) {
                    String key = (String) assignment.get("key");
                    Object value = assignment.get("value");
                    
                    // 支持表达式和动态值
                    if (value instanceof String) {
                        value = evaluateExpression((String) value, item);
                    }
                    
                    if (key != null) {
                        setNestedValue(result, key, value);
                    }
                }
            }
        }
        
        return result;
    }

    /**
     * 设置嵌套值，支持路径如 "user.name"
     */
    private void setNestedValue(Map<String, Object> data, String path, Object value) {
        String[] keys = path.split("\\.");
        Map<String, Object> current = data;
        
        for (int i = 0; i < keys.length - 1; i++) {
            String key = keys[i];
            if (!current.containsKey(key)) {
                current.put(key, new HashMap<String, Object>());
            }
            Object next = current.get(key);
            if (next instanceof Map) {
                current = (Map<String, Object>) next;
            } else {
                // 如果中间节点不是 Map，覆盖它
                Map<String, Object> newMap = new HashMap<>();
                current.put(key, newMap);
                current = newMap;
            }
        }
        
        String lastKey = keys[keys.length - 1];
        current.put(lastKey, value);
    }

    /**
     * 删除嵌套值
     */
    private void removeNestedValue(Map<String, Object> data, String path) {
        String[] keys = path.split("\\.");
        Map<String, Object> current = data;
        
        for (int i = 0; i < keys.length - 1; i++) {
            String key = keys[i];
            Object next = current.get(key);
            if (next instanceof Map) {
                current = (Map<String, Object>) next;
            } else {
                return; // 路径不存在
            }
        }
        
        String lastKey = keys[keys.length - 1];
        current.remove(lastKey);
    }

    /**
     * 评估表达式，支持 {{ }} 语法
     */
    private Object evaluateExpression(String expression, Map<String, Object> context) {
        if (!expression.contains("{{") || !expression.contains("}}")) {
            return expression;
        }
        
        // 简单实现：提取 {{ field }} 并从 context 中获取值
        String regex = "\\{\\{\\s*([a-zA-Z0-9_.]+)\\s*\\}\\}";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(expression);
        
        if (matcher.matches()) {
            String fieldPath = matcher.group(1);
            Object value = getNestedValue(context, fieldPath);
            return value != null ? value : expression;
        }
        
        return expression;
    }

    private Object getNestedValue(Map<String, Object> data, String path) {
        String[] keys = path.split("\\.");
        Object current = data;
        for (String key : keys) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(key);
            } else {
                return null;
            }
        }
        return current;
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
