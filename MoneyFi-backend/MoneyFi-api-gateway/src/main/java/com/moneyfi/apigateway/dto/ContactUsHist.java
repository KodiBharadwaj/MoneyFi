package com.moneyfi.apigateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactUsHist {
    private Long id;
    private Long contactUsId;
    private String name;
    private String message;
    private LocalDateTime updatedTime;
    private String requestReason;
    private String requestStatus;
}
