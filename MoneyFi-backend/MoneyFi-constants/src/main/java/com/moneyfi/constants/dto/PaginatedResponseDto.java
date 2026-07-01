package com.moneyfi.constants.dto;

import java.util.List;

public class PaginatedResponseDto<T> {

    private List<T> data;
    private Long totalCount;

    public void setData(List<T> data) {
        this.data = data;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    private PaginatedResponseDto(Builder<T> builder) {
        this.data = builder.data;
        this.totalCount = builder.totalCount;
    }

    public PaginatedResponseDto() {
    }

    public PaginatedResponseDto(List<T> data, Long totalCount) {
        this.data = data;
        this.totalCount = totalCount;
    }

    public List<T> getData() {
        return data;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {
        private List<T> data;
        private Long totalCount;

        public Builder<T> data(List<T> data) {
            this.data = data;
            return this;
        }

        public Builder<T> totalCount(Long totalCount) {
            this.totalCount = totalCount;
            return this;
        }

        public PaginatedResponseDto<T> build() {
            return new PaginatedResponseDto<>(this);
        }
    }
}
