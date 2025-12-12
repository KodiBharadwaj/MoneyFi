package com.moneyfi.transaction.service.income.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsForStatementDto {
    private String name;
    private String username;
    private String phoneNumber;
    private String address;
}
