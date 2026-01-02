package com.moneyfi.wealthcore.service.admin;

import com.moneyfi.wealthcore.service.admin.dto.request.CategoryRequestDto;
import jakarta.validation.Valid;

import java.util.List;

public interface AdminService {

    void saveCategoryWiseList(Long adminUserId, @Valid CategoryRequestDto requestDto);

    List<String> getCategoryType();

    void updateCategoryWiseList(Long adminUserId, Integer categoryId, @Valid CategoryRequestDto requestDto);

    void deleteCategoryWiseList(Long adminUserId, Integer categoryId);
}
