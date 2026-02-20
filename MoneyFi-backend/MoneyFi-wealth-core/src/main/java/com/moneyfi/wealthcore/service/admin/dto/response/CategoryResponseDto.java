package com.moneyfi.wealthcore.service.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer categoryId;
    private String type;
    private String category;
}
