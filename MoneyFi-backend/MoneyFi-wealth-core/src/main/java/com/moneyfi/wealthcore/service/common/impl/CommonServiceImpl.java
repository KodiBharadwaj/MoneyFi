package com.moneyfi.wealthcore.service.common.impl;

import com.moneyfi.wealthcore.model.common.CategoryListModel;
import com.moneyfi.wealthcore.repository.common.CategoryListRepository;
import com.moneyfi.wealthcore.service.admin.dto.response.CategoryResponseDto;
import com.moneyfi.wealthcore.service.common.CommonService;
import com.moneyfi.wealthcore.utils.constants.StringConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CommonServiceImpl implements CommonService {

    private final CategoryListRepository categoryListRepository;

    public CommonServiceImpl(CategoryListRepository categoryListRepository) {
        this.categoryListRepository = categoryListRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getCategoryWiseList(List<String> type) {
        if (ObjectUtils.isNotEmpty(type) && type.size() == 1 && type.get(0).equalsIgnoreCase("ALL")) {
            type.clear();
            type.addAll(StringConstants.getCategoryListEnum());
        }
        List<CategoryListModel> resultList = new ArrayList<>();
        type.forEach(categoryType -> resultList.addAll(categoryListRepository.findByType(categoryType)));
        return resultList
                .stream()
                .map(category -> new CategoryResponseDto(category.getId(), category.getType(), category.getCategory()))
                .sorted((a,b) -> a.getType().compareTo(b.getType()))
                .toList();
    }
}
