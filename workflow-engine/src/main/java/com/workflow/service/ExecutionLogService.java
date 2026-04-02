package com.workflow.service;

import com.workflow.model.ExecutionLog;
import com.workflow.model.WorkflowExecution;
import com.workflow.repository.ExecutionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 执行日志服务 (类似 n8n 的 Execution Log)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ExecutionLogService {

    private final ExecutionLogRepository executionLogRepository;

    public ExecutionLog logStart(WorkflowExecution execution, String nodeId, String nodeName, String inputData) {
        ExecutionLog log = new ExecutionLog();
        log.setExecution(execution);
        log.setNodeId(nodeId);
        log.setNodeName(nodeName);
        log.setStatus(ExecutionLog.ExecutionStatus.STARTED);
        log.setInputData(inputData);
        log.setStartedAt(LocalDateTime.now());
        return executionLogRepository.save(log);
    }

    public ExecutionLog logSuccess(ExecutionLog log, String outputData, long durationMs) {
        log.setStatus(ExecutionLog.ExecutionStatus.SUCCESS);
        log.setOutputData(outputData);
        log.setDurationMs(durationMs);
        log.setEndedAt(LocalDateTime.now());
        return executionLogRepository.save(log);
    }

    public ExecutionLog logError(ExecutionLog log, String errorMessage, long durationMs) {
        log.setStatus(ExecutionLog.ExecutionStatus.ERROR);
        log.setErrorMessage(errorMessage);
        log.setDurationMs(durationMs);
        log.setEndedAt(LocalDateTime.now());
        return executionLogRepository.save(log);
    }

    public ExecutionLog logSkip(ExecutionLog log) {
        log.setStatus(ExecutionLog.ExecutionStatus.SKIPPED);
        log.setEndedAt(LocalDateTime.now());
        return executionLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<ExecutionLog> getLogsByExecution(Long executionId) {
        return executionLogRepository.findByExecutionId(executionId);
    }

    @Transactional(readOnly = true)
    public List<ExecutionLog> getLogsByNode(String nodeId) {
        return executionLogRepository.findByNodeId(nodeId);
    }

    public void deleteLogsByExecution(Long executionId) {
        executionLogRepository.deleteByExecutionId(executionId);
    }
}
