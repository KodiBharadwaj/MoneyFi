package com.moneyfi.wealthcore.service.common;

import com.moneyfi.wealthcore.service.admin.dto.response.CategoryResponseDto;

import java.util.List;

public interface CommonService {
    List<CategoryResponseDto> getCategoryWiseList(List<String> type);
}
