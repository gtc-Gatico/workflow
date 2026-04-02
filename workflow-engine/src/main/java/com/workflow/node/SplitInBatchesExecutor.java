package com.workflow.node;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Split In Batches 节点执行器 (类似 n8n 的 Split In Batches Node)
 * 将大量数据分批处理，避免内存溢出或 API 限流
 */
public class SplitInBatchesExecutor implements NodeExecutor {

    @Override
    public String getNodeType() {
        return "splitInBatches";
    }

    @Override
    public String getDescription() {
        return "Split data into batches for processing";
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
        
        int batchSize = 10; // 默认每批 10 条
        if (properties != null && properties.get("batchSize") != null) {
            batchSize = ((Number) properties.get("batchSize")).intValue();
        }

        List<Map<String, Object>> items = new ArrayList<>();
        
        // 如果输入是列表，分割成批次
        if (inputData.containsKey("items") && inputData.get("items") instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> allItems = (List<Map<String, Object>>) inputData.get("items");
            
            Integer batchIndex = (Integer) context.getGlobalValue("batchIndex");
            if (batchIndex == null) {
                batchIndex = 0;
            }

            int startIndex = batchIndex * batchSize;
            int endIndex = Math.min(startIndex + batchSize, allItems.size());

            if (startIndex >= allItems.size()) {
                // 所有批次已处理完成
                NodeExecutionResult result = NodeExecutionResult.success(new HashMap<>());
                result.addMetadata("noMoreItems", true);
                return result;
            }

            items = new ArrayList<>(allItems.subList(startIndex, endIndex));
            
            // 设置下一批次的索引
            context.setGlobalValue("batchIndex", batchIndex + 1);
        } else {
            // 单个项目，直接返回
            items.add(inputData);
        }

        NodeExecutionResult result = NodeExecutionResult.success(items);
        result.addMetadata("batchSize", items.size());
        result.addMetadata("hasMoreItems", true);
        
        return result;
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
