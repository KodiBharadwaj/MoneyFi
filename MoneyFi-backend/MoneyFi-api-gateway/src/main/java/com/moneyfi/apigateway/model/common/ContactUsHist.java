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
@Table(name = "contact_us_table_hist")
public class ContactUsHist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long contactUsId;
    private String name;
    private String message;
    private LocalDateTime updatedTime;
    private String requestReason;
    private String requestStatus;

    public ContactUsHist(Long contactUsId, String name, String message, LocalDateTime updatedTime, String requestReason, String requestStatus) {
        this.contactUsId = contactUsId;
        this.name = name;
        this.message = message;
        this.updatedTime = updatedTime;
        this.requestReason = requestReason;
        this.requestStatus = requestStatus;
    }
}
