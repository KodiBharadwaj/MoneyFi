package com.moneyfi.apigateway.model.gmailsync;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

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
    private Instant processedAt = Instant.now();
}

