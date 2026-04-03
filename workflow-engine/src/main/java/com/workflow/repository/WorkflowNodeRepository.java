package com.workflow.repository;

import com.workflow.model.WorkflowNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface WorkflowNodeRepository extends JpaRepository<WorkflowNode, Long> {
    List<WorkflowNode> findByWorkflowId(Long workflowId);
    List<WorkflowNode> findByWorkflowIdAndActive(Long workflowId, boolean active);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM WorkflowNode n WHERE n.workflow.id = :workflowId")
    void deleteByWorkflowId(@Param("workflowId") Long workflowId);
}
