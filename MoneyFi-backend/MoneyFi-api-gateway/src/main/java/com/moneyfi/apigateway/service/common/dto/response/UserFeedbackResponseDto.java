package com.moneyfi.apigateway.service.common.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

import static com.moneyfi.apigateway.util.constants.StringUtils.DATE_TIME_PATTERN;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFeedbackResponseDto {
    private Long feedbackId;
    @JsonIgnore
    private String description;
    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private Timestamp timeOfFeedback;
    private int rating;
    private String message;
}
