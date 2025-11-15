package com.moneyfi.user.service.admin.dto.response;

import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDefectTrackingForAdminDto {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String referenceNumber;
    private String status;
    private Long defectId;

    @PrePersist
    public void setTimeFunction() {
        this.setEndTime(null);
    }
}