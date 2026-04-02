package com.workflow.node;

import java.util.Map;
import java.util.List;

/**
 * Switch 节点执行器 (类似 n8n 的 Switch Node)
 * 根据条件将数据分发到不同的分支
 */
public class SwitchExecutor implements NodeExecutor {

    @Override
    public String getNodeType() {
        return "switch";
    }

    @Override
    public String getDescription() {
        return "Route items to different branches based on conditions";
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
            return NodeExecutionResult.failure("No node properties configured");
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rules = (List<Map<String, Object>>) properties.get("rules");
        
        if (rules == null || rules.isEmpty()) {
            return NodeExecutionResult.success(inputData);
        }

        // 评估每个规则，决定走哪个分支
        int matchedBranch = -1;
        for (int i = 0; i < rules.size(); i++) {
            Map<String, Object> rule = rules.get(i);
            if (evaluateRule(rule, inputData)) {
                matchedBranch = i;
                break;
            }
        }

        // 如果没有匹配的规则，检查是否有默认分支
        if (matchedBranch == -1) {
            Boolean hasDefault = (Boolean) properties.get("hasDefault");
            if (hasDefault != null && hasDefault) {
                matchedBranch = rules.size(); // 默认分支是最后一个
            }
        }

        NodeExecutionResult result = NodeExecutionResult.success(inputData);
        result.addMetadata("matchedBranch", matchedBranch);
        
        return result;
    }

    /**
     * 评估规则
     */
    private boolean evaluateRule(Map<String, Object> rule, Map<String, Object> data) {
        String operation = (String) rule.get("operation");
        String key = (String) rule.get("key");
        Object value = rule.get("value");

        if (key == null || operation == null) {
            return false;
        }

        Object actualValue = data.get(key);
        if (actualValue == null) {
            // 支持嵌套路径，如 "user.name"
            if (key.contains(".")) {
                actualValue = getNestedValue(data, key);
            }
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

    @Override
    public List<String> getOutputTypes() {
        return List.of("*");
    }
}
