package com.moneyfi.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

@Getter
public class ReusableTotalCountDto {
    @JsonIgnore
    private Long totalCount;
}
