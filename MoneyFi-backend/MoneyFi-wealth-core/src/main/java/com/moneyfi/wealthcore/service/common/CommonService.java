package com.moneyfi.wealthcore.service.common;

import com.moneyfi.constants.dto.CategoryResponseDto;

import java.util.List;

public interface CommonService {
    List<CategoryResponseDto> getCategoryWiseList(List<String> type);
}
