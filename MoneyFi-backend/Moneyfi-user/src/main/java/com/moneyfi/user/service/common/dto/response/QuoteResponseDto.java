package com.moneyfi.user.service.common.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuoteResponseDto {
    @JsonProperty("q")
    private String quote;
    @JsonProperty("a")
    private String author;
    @JsonProperty("h")
    private String description;
}
