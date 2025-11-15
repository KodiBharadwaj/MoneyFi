package com.moneyfi.user.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthHist {
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
