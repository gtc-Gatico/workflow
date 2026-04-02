package com.workflow.repository;

import com.workflow.model.WorkflowExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowExecutionRepository extends JpaRepository<WorkflowExecution, Long> {
    List<WorkflowExecution> findByWorkflowId(Long workflowId);
    List<WorkflowExecution> findByWorkflowIdAndStatus(Long workflowId, String status);
}
