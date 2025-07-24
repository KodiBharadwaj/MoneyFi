package com.moneyfi.apigateway.model.common;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "contact_us_table")
public class ContactUs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String message;
    private String imageId;
    private String referenceNumber;
    private boolean isRequestActive;
    private String requestReason;
    private boolean isVerified;
    private String requestStatus;
}
