package com.moneyfi.user.service.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminScheduleRequestDto {
    @NotNull
    private Long scheduleId;
    @NotBlank
    private String subject;
    @NotBlank
    private String description;
    @NotBlank
    private String recipients;
    @NotNull
    private LocalDateTime scheduleFrom;
    @NotNull
    private LocalDateTime scheduleTo;
}
