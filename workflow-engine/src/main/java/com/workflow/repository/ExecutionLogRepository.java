package com.workflow.repository;

import com.workflow.model.ExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExecutionLogRepository extends JpaRepository<ExecutionLog, Long> {
    List<ExecutionLog> findByExecutionId(Long executionId);
    List<ExecutionLog> findByNodeId(String nodeId);
    void deleteByExecutionId(Long executionId);
}
