package com.workflow.node;

/**
 * Base interface for all workflow node executors
 */
public interface NodeExecutor {
    
    /**
     * Get the node type this executor handles
     */
    String getNodeType();
    
    /**
     * Execute the node with the given input data
     * @param config JSON configuration for the node
     * @param inputData Input data from previous node or trigger
     * @return Execution result
     */
    NodeExecutionResult execute(String config, String inputData);
}
