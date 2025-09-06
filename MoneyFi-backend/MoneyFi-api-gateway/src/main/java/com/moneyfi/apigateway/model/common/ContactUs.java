package com.moneyfi.apigateway.model.common;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "contact_us_table")
public class ContactUs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String imageId;
    private String referenceNumber;
    private boolean isRequestActive;
    private String requestReason;
    private boolean isVerified;
    private String requestStatus;
    private LocalDateTime startTime;
    private LocalDateTime completedTime;
}
