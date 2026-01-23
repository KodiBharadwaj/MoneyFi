package com.moneyfi.apigateway.model.gmailsync;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "gmail_sync_history")
public class GmailSyncHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime syncTime;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long userId;

    public GmailSyncHistory(LocalDateTime syncTime, Long userId) {
        this.syncTime = syncTime;
        this.userId = userId;
    }
}
