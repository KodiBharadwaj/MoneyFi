package com.moneyfi.apigateway.service.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuoteResponseDto {
    private String quote;
    private String author;
}
