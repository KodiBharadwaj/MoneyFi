package com.moneyfi.user.service.admin.dto.response;

import com.moneyfi.user.dto.ReusableTotalCountDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDefectResponseDto extends ReusableTotalCountDto {
    private Long defectId;
    private String name;
    private String username;
    private String referenceNumber;
    private String description;
    private String defectStatus;
}
