package com.workflow.engine.expression;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 表达式引擎 - 类似 n8n 的表达式语法 {{ }}
 * 支持：变量引用、数学运算、字符串操作、日期处理、JSON 路径
 */
public class ExpressionEngine {
    
    private static final Pattern EXPRESSION_PATTERN = Pattern.compile("\\{\\{\\s*(.+?)\\s*\\}\\}");
    private static final Pattern JSON_PATH_PATTERN = Pattern.compile("\\$(\\.[\\w]+|\\[[\\d]+\\])+");
    
    /**
     * 解析表达式，替换所有 {{ }} 中的内容
     */
    public static String evaluate(String expression, Map<String, Object> context) {
        if (expression == null || !expression.contains("{{")) {
            return expression;
        }
        
        Matcher matcher = EXPRESSION_PATTERN.matcher(expression);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String expr = matcher.group(1).trim();
            Object value = evaluateExpression(expr, context);
            matcher.appendReplacement(result, value != null ? value.toString() : "");
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * 计算单个表达式
     */
    public static Object evaluateExpression(String expr, Map<String, Object> context) {
        try {
            // JSON 路径查询
            if (expr.startsWith("$.")) {
                return queryJsonPath(expr, context);
            }
            
            // 直接变量引用
            if (!expr.contains(" ") && !isOperator(expr.charAt(0))) {
                return context.get(expr);
            }
            
            // 数学表达式
            if (containsMathOperator(expr)) {
                return evaluateMathExpression(expr, context);
            }
            
            // 字符串操作
            if (expr.contains("+") && expr.contains("\"")) {
                return evaluateStringExpression(expr, context);
            }
            
            // 三元表达式
            if (expr.contains("?") && expr.contains(":")) {
                return evaluateTernaryExpression(expr, context);
            }
            
            // 函数调用
            if (expr.contains("(") && expr.contains(")")) {
                return evaluateFunction(expr, context);
            }
            
            return context.get(expr);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to evaluate expression: " + expr, e);
        }
    }
    
    /**
     * JSON 路径查询
     */
    public static Object queryJsonPath(String jsonPath, Map<String, Object> context) {
        Object current = context;
        String[] parts = jsonPath.substring(2).split("\\.(?![^\\[]*\\])");
        
        for (String part : parts) {
            if (current == null) {
                return null;
            }
            
            // 数组索引
            if (part.contains("[")) {
                String key = part.substring(0, part.indexOf("["));
                if (!key.isEmpty() && current instanceof Map) {
                    current = ((Map<?, ?>) current).get(key);
                }
                
                // 提取索引
                Pattern indexPattern = Pattern.compile("\\[(\\d+)\\]");
                Matcher matcher = indexPattern.matcher(part);
                while (matcher.find()) {
                    int index = Integer.parseInt(matcher.group(1));
                    if (current instanceof java.util.List) {
                        java.util.List<?> list = (java.util.List<?>) current;
                        current = index < list.size() ? list.get(index) : null;
                    } else {
                        return null;
                    }
                }
            } else {
                if (current instanceof Map) {
                    current = ((Map<?, ?>) current).get(part);
                } else {
                    return null;
                }
            }
        }
        
        return current;
    }
    
    /**
     * 数学表达式求值
     */
    private static Object evaluateMathExpression(String expr, Map<String, Object> context) {
        // 简单实现，生产环境建议使用专门的表达式引擎如 Aviator、MVEL
        expr = replaceVariables(expr, context);
        try {
            // 使用 JavaScript 引擎计算
            javax.script.ScriptEngine engine = 
                new javax.script.ScriptEngineManager().getEngineByName("JavaScript");
            return engine.eval(expr);
        } catch (Exception e) {
            throw new RuntimeException("Failed to evaluate math expression: " + expr, e);
        }
    }
    
    /**
     * 字符串表达式求值
     */
    private static String evaluateStringExpression(String expr, Map<String, Object> context) {
        expr = replaceVariables(expr, context);
        // 移除引号并连接
        return expr.replace("\"", "");
    }
    
    /**
     * 三元表达式求值
     */
    private static Object evaluateTernaryExpression(String expr, Map<String, Object> context) {
        // 简单实现：condition ? trueValue : falseValue
        String[] parts = expr.split("\\?");
        if (parts.length != 2) {
            return null;
        }
        
        String condition = parts[0].trim();
        String[] rest = parts[1].split(":");
        if (rest.length != 2) {
            return null;
        }
        
        String trueValue = rest[0].trim();
        String falseValue = rest[1].trim();
        
        Object condResult = evaluateExpression(condition, context);
        boolean isTrue = condResult instanceof Boolean ? (Boolean) condResult : condResult != null;
        
        return isTrue ? evaluateExpression(trueValue, context) : evaluateExpression(falseValue, context);
    }
    
    /**
     * 函数调用求值
     */
    private static Object evaluateFunction(String expr, Map<String, Object> context) {
        int parenIndex = expr.indexOf("(");
        String funcName = expr.substring(0, parenIndex).trim();
        String argsStr = expr.substring(parenIndex + 1, expr.lastIndexOf(")")).trim();
        
        // 解析参数
        String[] args = argsStr.isEmpty() ? new String[0] : argsStr.split(",");
        
        switch (funcName) {
            case "upper":
            case "toUpperCase":
                return getArg(args, 0, context).toString().toUpperCase();
            case "lower":
            case "toLowerCase":
                return getArg(args, 0, context).toString().toLowerCase();
            case "length":
                Object obj = getArg(args, 0, context);
                return obj instanceof String ? ((String) obj).length() : 
                       obj instanceof java.util.Collection ? ((java.util.Collection<?>) obj).size() : 0;
            case "substring":
                String str = getArg(args, 0, context).toString();
                int start = Integer.parseInt(getArg(args, 1, context).toString());
                int end = args.length > 2 ? Integer.parseInt(getArg(args, 2, context).toString()) : str.length();
                return str.substring(start, end);
            case "replace":
                return getArg(args, 0, context).toString()
                    .replace(getArg(args, 1, context).toString(), getArg(args, 2, context).toString());
            case "split":
                return java.util.List.of(getArg(args, 0, context).toString()
                    .split(getArg(args, 1, context).toString()));
            case "join":
                Object arr = getArg(args, 0, context);
                String delimiter = args.length > 1 ? getArg(args, 1, context).toString() : ",";
                if (arr instanceof java.util.List) {
                    return String.join(delimiter, (java.util.List<String>) arr);
                }
                return arr.toString();
            case "now":
                return java.time.Instant.now().toString();
            case "formatDate":
                Object dateObj = getArg(args, 0, context);
                String format = args.length > 1 ? getArg(args, 1, context).toString() : "yyyy-MM-dd";
                java.time.format.DateTimeFormatter formatter = 
                    java.time.format.DateTimeFormatter.ofPattern(format);
                return java.time.LocalDateTime.parse(dateObj.toString()).format(formatter);
            case "parseInt":
                return Integer.parseInt(getArg(args, 0, context).toString());
            case "parseFloat":
                return Double.parseDouble(getArg(args, 0, context).toString());
            default:
                // 未知函数，返回 null
                return null;
        }
    }
    
    private static Object getArg(String[] args, int index, Map<String, Object> context) {
        if (index >= args.length) {
            return null;
        }
        String arg = args[index].trim();
        // 如果是变量引用，从上下文获取
        if (!arg.startsWith("\"") && !arg.startsWith("'") && !arg.matches("\\d+")) {
            return context.get(arg);
        }
        // 去除引号
        return arg.replaceAll("^['\"]|['\"]$", "");
    }
    
    /**
     * 替换表达式中的变量
     */
    private static String replaceVariables(String expr, Map<String, Object> context) {
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            expr = expr.replace(key, value != null ? value.toString() : "null");
        }
        return expr;
    }
    
    private static boolean containsMathOperator(String expr) {
        return expr.matches(".*[+\\-*/%].*");
    }
    
    private static boolean isOperator(char c) {
        return "+-*/%".indexOf(c) >= 0;
    }
}
