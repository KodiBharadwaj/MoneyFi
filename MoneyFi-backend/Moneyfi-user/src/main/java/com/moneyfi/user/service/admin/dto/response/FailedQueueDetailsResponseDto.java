package com.moneyfi.user.service.admin.dto.response;

import com.moneyfi.user.dto.ReusableTotalCountDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
public class FailedQueueDetailsResponseDto extends ReusableTotalCountDto {
    private Long failedQueueId;
    private String mdcRequestId;
    private String referenceNumber;
    private String username;
    private Long userId;
    private String name;
    private String message;
    private LocalDateTime failureTime;
}
