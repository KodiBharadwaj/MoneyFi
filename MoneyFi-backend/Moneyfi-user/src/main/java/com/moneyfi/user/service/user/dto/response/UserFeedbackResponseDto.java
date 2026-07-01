package com.moneyfi.user.service.user.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.moneyfi.user.dto.ReusableTotalCountDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFeedbackResponseDto extends ReusableTotalCountDto {
    private int id;
    private Long feedbackId;
    @JsonIgnore
    private String description;
    private Timestamp timeOfFeedback;
    private int rating;
    private String message;
}
