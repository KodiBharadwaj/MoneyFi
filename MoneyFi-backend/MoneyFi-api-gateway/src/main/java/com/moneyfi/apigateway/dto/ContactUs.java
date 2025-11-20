package com.moneyfi.apigateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactUs {
    private Long id;
    private String email;
    private String referenceNumber;
    private boolean isRequestActive;
    private String requestReason;
    private boolean isVerified;
    private String requestStatus;
    private LocalDateTime startTime;
    private LocalDateTime completedTime;
}
