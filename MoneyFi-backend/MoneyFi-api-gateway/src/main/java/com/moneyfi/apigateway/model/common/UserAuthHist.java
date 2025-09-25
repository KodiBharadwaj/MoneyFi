package com.moneyfi.apigateway.model.common;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_auth_hist_table")
public class UserAuthHist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private LocalDateTime updatedTime;
    private int reasonTypeId;
    private String comment;
    private Long updatedBy;

    public UserAuthHist(Long userId, LocalDateTime updatedTime, Integer reasonTypeId, String comment, Long updatedBy) {
        this.userId = userId;
        this.updatedTime = updatedTime;
        this.reasonTypeId = reasonTypeId;
        this.comment = comment;
        this.updatedBy = updatedBy;
    }
}
