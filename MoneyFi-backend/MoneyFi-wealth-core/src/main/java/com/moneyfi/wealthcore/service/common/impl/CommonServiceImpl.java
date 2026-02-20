package com.moneyfi.wealthcore.service.common.impl;

import com.moneyfi.wealthcore.model.common.CategoryListModel;
import com.moneyfi.wealthcore.repository.common.CategoryListRepository;
import com.moneyfi.wealthcore.service.admin.dto.response.CategoryResponseDto;
import com.moneyfi.wealthcore.service.common.CommonService;
import com.moneyfi.wealthcore.utils.constants.StringConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CommonServiceImpl implements CommonService {

    private final CategoryListRepository categoryListRepository;

    public CommonServiceImpl(CategoryListRepository categoryListRepository) {
        this.categoryListRepository = categoryListRepository;
    }

    @Override
    @Cacheable(value = "categoryList", key = "T(String).join('-', #type)")
    public List<CategoryResponseDto> getCategoryWiseList(List<String> type) {
        List<String> resolvedType = type;
        if (ObjectUtils.isNotEmpty(type) && type.size() == 1 && type.get(0).equalsIgnoreCase("ALL")) {
            resolvedType = new ArrayList<>(StringConstants.getCategoryListEnum());
        }
        List<CategoryListModel> resultList = categoryListRepository.findByTypeIn(resolvedType);

        return resultList.stream()
                .map(category -> new CategoryResponseDto(category.getId(), category.getType(), category.getCategory()))
                .sorted(Comparator.comparing(CategoryResponseDto::getType))
                .collect(Collectors.toList());
    }
}
