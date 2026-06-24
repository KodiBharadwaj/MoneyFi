package com.moneyfi.user.service.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaginationRequestDto {
    @NotNull
    private Long offset;
    @NotNull
    private Long limit;
    private String search;
    private String searchBy;
}
