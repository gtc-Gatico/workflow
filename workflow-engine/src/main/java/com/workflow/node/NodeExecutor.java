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
     * @throws Exception 执行异常
     */
    NodeExecutionResult execute(NodeExecutionContext context) throws Exception;

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
