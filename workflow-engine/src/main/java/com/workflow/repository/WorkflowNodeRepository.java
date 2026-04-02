package com.workflow.repository;

import com.workflow.model.WorkflowNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowNodeRepository extends JpaRepository<WorkflowNode, Long> {
    List<WorkflowNode> findByWorkflowId(Long workflowId);
    List<WorkflowNode> findByWorkflowIdAndActive(Long workflowId, boolean active);
}
