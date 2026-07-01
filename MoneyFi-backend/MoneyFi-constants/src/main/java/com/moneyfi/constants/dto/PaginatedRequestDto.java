package com.moneyfi.constants.dto;

import java.util.Objects;

public class PaginatedRequestDto {
    private Long offset;
    private Long limit;
    private String search;
    private String searchBy;
    private String sortBy;
    private String sortOrder;

    public Long getOffset() {
        return offset;
    }

    public void setOffset(Long offset) {
        this.offset = Objects.requireNonNull(offset, "offset cannot be null");
    }

    public void setLimit(Long limit) {
        this.limit = Objects.requireNonNull(limit, "limit cannot be null");
    }

    public Long getLimit() {
        return limit;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getSearchBy() {
        return searchBy;
    }

    public void setSearchBy(String searchBy) {
        this.searchBy = searchBy;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }
}
