package com.workflow.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 凭证管理 (类似 n8n 的 Credentials)
 * 用于安全存储第三方服务的 API Key, Username/Password 等
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "credentials")
public class Credential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String type; // e.g., "httpBasicAuth", "oAuth2", "awsAccessKey"

    @Column(columnDefinition = "TEXT")
    private String data; // JSON encrypted data

    @Column(length = 1000)
    private String description;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
