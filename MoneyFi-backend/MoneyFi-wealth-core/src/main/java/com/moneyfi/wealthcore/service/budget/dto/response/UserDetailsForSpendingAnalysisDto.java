package com.moneyfi.wealthcore.service.budget.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsForSpendingAnalysisDto {
    private String name;
    private String username;
    private String phoneNumber;
    private String address;
}