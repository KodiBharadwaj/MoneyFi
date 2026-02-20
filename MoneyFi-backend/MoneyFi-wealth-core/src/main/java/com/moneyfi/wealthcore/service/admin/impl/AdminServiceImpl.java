package com.moneyfi.wealthcore.service.admin.impl;

import com.moneyfi.wealthcore.exceptions.ResourceNotFoundException;
import com.moneyfi.wealthcore.model.common.CategoryListModel;
import com.moneyfi.wealthcore.repository.common.CategoryListRepository;
import com.moneyfi.wealthcore.service.admin.AdminService;
import com.moneyfi.wealthcore.service.admin.dto.request.CategoryRequestDto;
import com.moneyfi.wealthcore.utils.constants.StringConstants;
import com.moneyfi.wealthcore.validator.AdminValidations;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final CategoryListRepository categoryListRepository;

    public AdminServiceImpl(CategoryListRepository categoryListRepository){
        this.categoryListRepository = categoryListRepository;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    @CacheEvict(value = "categoryList", allEntries = true)
    public void saveCategoryWiseList(Long adminUserId, CategoryRequestDto requestDto) {
        AdminValidations.validateInputCategoryList(adminUserId, requestDto);
        CategoryListModel categoryListModel = new CategoryListModel();
        BeanUtils.copyProperties(requestDto, categoryListModel);
        categoryListModel.setUpdatedBy(adminUserId);
        categoryListRepository.save(categoryListModel);
    }

    @Override
    public List<String> getCategoryType() {
        return StringConstants.getCategoryListEnum();
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    @CacheEvict(value = "categoryList", allEntries = true)
    public void updateCategoryWiseList(Long adminUserId, Integer categoryId, CategoryRequestDto requestDto) {
        CategoryListModel categoryModel = categoryListRepository.findById(categoryId).orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        AdminValidations.validateInputCategoryList(adminUserId, requestDto);
        categoryModel.setUpdatedBy(adminUserId);
        categoryModel.setUpdatedAt(LocalDateTime.now());
        categoryModel.setCategory(requestDto.getCategory());
        categoryListRepository.save(categoryModel);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    @CacheEvict(value = "categoryList", allEntries = true)
    public void deleteCategoryWiseList(Long adminUserId, Integer categoryId) {
        categoryListRepository.findById(categoryId).orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        categoryListRepository.deleteById(categoryId);
    }
}
