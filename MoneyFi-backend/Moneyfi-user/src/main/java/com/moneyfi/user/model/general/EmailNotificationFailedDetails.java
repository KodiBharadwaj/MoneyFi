package com.moneyfi.user.model.general;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "email_notification_failed_details_table")
public class EmailNotificationFailedDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mdc_request_id")
    private String mdcRequestId;

    @Column(name = "reference_number")
    private String referenceNumber;

    @NotBlank
    @Column(name = "message")
    private String message;

    @NotBlank
    @Column(name = "username")
    private String username;

    @NotNull
    @Column(name = "user_id")
    private String userId;

    @Column(name = "name")
    private String name;

    @NotNull
    @Column(name = "created_by")
    private String createdBy;

    @NotNull
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updatedBy")
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
