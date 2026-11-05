package com.moneyfi.user.service.primary.user.dto.emaildto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatementAnalysisDto {
    private String name;
    private String username;
    private byte[] pdfByte;
}
