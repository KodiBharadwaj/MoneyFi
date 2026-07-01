package com.moneyfi.constants.dto;

import java.io.Serializable;

public class CategoryResponseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer categoryId;
    private String type;
    private String category;

    public CategoryResponseDto() {
    }

    public CategoryResponseDto(Integer categoryId, String type, String category) {
        this.categoryId = categoryId;
        this.type = type;
        this.category = category;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "CategoryResponseDto{" +
                "categoryId=" + categoryId +
                ", type='" + type + '\'' +
                ", category='" + category + '\'' +
                '}';
    }

}
