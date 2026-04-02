package com.workflow.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 执行日志 (类似 n8n 的 Execution Log)
 * 记录每个节点执行的详细输入输出、错误信息、耗时等
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "execution_logs")
public class ExecutionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "execution_id", nullable = false)
    private WorkflowExecution execution;

    @Column(nullable = false)
    private String nodeId;

    @Column(nullable = false)
    private String nodeName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExecutionStatus status; // STARTED, SUCCESS, ERROR

    @Column(columnDefinition = "TEXT")
    private String inputData; // JSON

    @Column(columnDefinition = "TEXT")
    private String outputData; // JSON

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private Long durationMs; // 执行耗时 (毫秒)

    @Column(updatable = false)
    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    @PrePersist
    protected void onCreate() {
        startedAt = LocalDateTime.now();
    }

    public enum ExecutionStatus {
        STARTED, SUCCESS, ERROR, SKIPPED
    }
}
