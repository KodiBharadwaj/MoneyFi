package com.moneyfi.wealthcore.validator;

import com.moneyfi.wealthcore.exceptions.ScenarioNotPossibleException;
import com.moneyfi.wealthcore.service.admin.dto.request.CategoryRequestDto;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

public class AdminValidations {

    private AdminValidations() {}

    public static void validateInputCategoryList(Long adminUserId, CategoryRequestDto requestDto) {
        if(ObjectUtils.isEmpty(adminUserId)) {
            throw new ScenarioNotPossibleException("Admin user id is empty");
        }
        if(StringUtils.isBlank(requestDto.getType())) {
            throw new ScenarioNotPossibleException("Type should not be null");
        }
        if(StringUtils.isBlank(requestDto.getCategory())) {
            throw new ScenarioNotPossibleException("Category should not be null");
        }
    }
}
