package com.moneyfi.user.service.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NameChangeRequestDto {
    @NotBlank
    private String email;
    @NotBlank
    private String oldName;
    @NotBlank
    private String newName;
    @NotBlank
    private String referenceNumber;
    @NotBlank
    private String description;
}
