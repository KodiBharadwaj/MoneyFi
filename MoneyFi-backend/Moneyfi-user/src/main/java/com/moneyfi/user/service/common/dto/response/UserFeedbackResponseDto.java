package com.moneyfi.user.service.common.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFeedbackResponseDto {
    private int id;
    private Long feedbackId;
    @JsonIgnore
    private String description;
    private Timestamp timeOfFeedback;
    private int rating;
    private String message;
}
