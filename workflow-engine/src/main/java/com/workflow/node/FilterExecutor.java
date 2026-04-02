package com.workflow.node;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Filter 节点执行器 (类似 n8n 的 Filter Node)
 * 根据条件过滤数据
 */
public class FilterExecutor implements NodeExecutor {

    @Override
    public String getNodeType() {
        return "filter";
    }

    @Override
    public String getDescription() {
        return "Filter items based on conditions";
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

        String mode = (String) properties.getOrDefault("mode", "keep"); // keep or remove
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> conditions = (List<Map<String, Object>>) properties.get("conditions");
        
        if (conditions == null || conditions.isEmpty()) {
            return NodeExecutionResult.success(inputData);
        }

        List<Map<String, Object>> items = new ArrayList<>();
        
        // 如果输入是列表，过滤每个项目
        if (inputData.containsKey("items") && inputData.get("items") instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> allItems = (List<Map<String, Object>>) inputData.get("items");
            
            for (Map<String, Object> item : allItems) {
                boolean matches = evaluateConditions(conditions, item);
                
                if ("keep".equals(mode) && matches) {
                    items.add(item);
                } else if ("remove".equals(mode) && !matches) {
                    items.add(item);
                }
            }
        } else {
            // 单个项目
            boolean matches = evaluateConditions(conditions, inputData);
            if (("keep".equals(mode) && matches) || ("remove".equals(mode) && !matches)) {
                items.add(inputData);
            }
        }

        NodeExecutionResult result = NodeExecutionResult.success(items);
        result.addMetadata("filteredCount", items.size());
        
        return result;
    }

    /**
     * 评估所有条件
     */
    private boolean evaluateConditions(List<Map<String, Object>> conditions, Map<String, Object> item) {
        String operation = (String) conditions.get(0).getOrDefault("operation", "AND");
        
        if ("OR".equals(operation)) {
            return conditions.stream().anyMatch(condition -> evaluateCondition(condition, item));
        } else {
            // 默认 AND
            return conditions.stream().allMatch(condition -> evaluateCondition(condition, item));
        }
    }

    /**
     * 评估单个条件
     */
    private boolean evaluateCondition(Map<String, Object> condition, Map<String, Object> item) {
        String key = (String) condition.get("key");
        String operation = (String) condition.get("operation");
        Object value = condition.get("value");

        if (key == null) {
            return false;
        }

        Object actualValue = item.get(key);
        if (actualValue == null && key.contains(".")) {
            actualValue = getNestedValue(item, key);
        }

        if (actualValue == null) {
            return "exists".equals(operation) ? false : "notExists".equals(operation);
        }

        switch (operation) {
            case "equals":
                return String.valueOf(actualValue).equals(String.valueOf(value));
            case "notEquals":
                return !String.valueOf(actualValue).equals(String.valueOf(value));
            case "contains":
                return String.valueOf(actualValue).contains(String.valueOf(value));
            case "startsWith":
                return String.valueOf(actualValue).toString().startsWith(String.valueOf(value));
            case "endsWith":
                return String.valueOf(actualValue).toString().endsWith(String.valueOf(value));
            case "greaterThan":
                return compareValues(actualValue, value) > 0;
            case "lessThan":
                return compareValues(actualValue, value) < 0;
            case "exists":
                return actualValue != null;
            case "notExists":
                return actualValue == null;
            case "regex":
                return String.valueOf(actualValue).matches(String.valueOf(value));
            case "between":
                return isBetween(actualValue, condition.get("min"), condition.get("max"));
            default:
                return false;
        }
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

    private int compareValues(Object actual, Object expected) {
        try {
            double actualNum = Double.parseDouble(String.valueOf(actual));
            double expectedNum = Double.parseDouble(String.valueOf(expected));
            return Double.compare(actualNum, expectedNum);
        } catch (NumberFormatException e) {
            return String.valueOf(actual).compareTo(String.valueOf(expected));
        }
    }

    private boolean isBetween(Object value, Object min, Object max) {
        try {
            double num = Double.parseDouble(String.valueOf(value));
            double minNum = min != null ? Double.parseDouble(String.valueOf(min)) : Double.NEGATIVE_INFINITY;
            double maxNum = max != null ? Double.parseDouble(String.valueOf(max)) : Double.POSITIVE_INFINITY;
            return num >= minNum && num <= maxNum;
        } catch (NumberFormatException e) {
            return false;
        }
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
