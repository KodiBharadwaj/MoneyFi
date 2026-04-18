package com.moneyfi.wealthcore.service.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequestDto {
    @NotBlank
    private String type;
    @NotBlank
    private String category;
}
