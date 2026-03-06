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
    private LocalDateTime pendTime;
    private LocalDateTime endTime;
    private String referenceNumber;
    private String status;
    private String description;
    private StringBuilder adminRemarks = new StringBuilder();
    private Long defectId;
    private StringBuilder requestDoneBy = new StringBuilder();

    @PrePersist
    public void setTimeFunction() {
        this.setEndTime(null);
    }
}