package com.moneyfi.constants.dto;

import java.util.List;

public class BuildResponseDto<T> {

    private List<T> data;
    private String message;

    public BuildResponseDto() {
    }

    private BuildResponseDto(Builder<T> builder) {
        this.data = builder.data;
        this.message = builder.message;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {
        private List<T> data;
        private String message;

        public Builder<T> data(List<T> data) {
            this.data = data;
            return this;
        }

        public Builder<T> message(String message) {
            this.message = message;
            return this;
        }

        public BuildResponseDto<T> build() {
            return new BuildResponseDto<>(this);
        }
    }
}
