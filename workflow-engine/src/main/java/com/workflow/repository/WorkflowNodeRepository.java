package com.workflow.repository;

import com.workflow.model.WorkflowNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowNodeRepository extends JpaRepository<WorkflowNode, String> {
    List<WorkflowNode> findByWorkflowId(Long workflowId);
    List<WorkflowNode> findByWorkflowIdAndActive(Long workflowId, boolean active);
    
    @Modifying
    @Query("DELETE FROM WorkflowNode n WHERE n.workflow.id = ?1")
    void deleteByWorkflowId(Long workflowId);
}
