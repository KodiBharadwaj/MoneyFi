package com.moneyfi.user.model.primary.general;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(name = "value_json")
    private String valueJson;

    @NotBlank
    @Column(name = "username")
    private String username;

    @Column(name = "name")
    private String name;

    @NotBlank
    @Column(name = "failure_stage")
    private String failureStage;

    @NotBlank
    @Column(name = "message_type")
    private String messageType;

    @NotBlank
    @Column(name = "broker_used")
    private String brokerUsed;

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
