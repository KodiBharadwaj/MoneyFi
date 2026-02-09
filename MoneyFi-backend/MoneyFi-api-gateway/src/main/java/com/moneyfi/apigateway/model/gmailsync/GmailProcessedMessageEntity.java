package com.moneyfi.apigateway.model.gmailsync;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "gmail_processed_message")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GmailProcessedMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", nullable = false)
    private String messageId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_verified")
    private boolean isVerified;

    @PrePersist
    public void init() {
        this.processedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isVerified = false;
    }
}

