package com.moneyfi.wealthcore.service.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponseDto {
    private Integer categoryId;
    private String type;
    private String category;
}
