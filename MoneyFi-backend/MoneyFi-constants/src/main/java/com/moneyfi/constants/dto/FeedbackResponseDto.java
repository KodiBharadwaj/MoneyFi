package com.moneyfi.constants.dto;

import java.io.Serializable;

public class FeedbackResponseDto implements Serializable {

    private NotificationQueueDto requestValueJson;
    private String errorMessage;

    public FeedbackResponseDto() {
    }

    private FeedbackResponseDto(Builder builder) {
        this.requestValueJson = builder.requestValueJson;
        this.errorMessage = builder.errorMessage;
    }

    public NotificationQueueDto getRequestValueJson() {
        return requestValueJson;
    }

    public void setRequestValueJson(NotificationQueueDto requestValueJson) {
        this.requestValueJson = requestValueJson;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private NotificationQueueDto requestValueJson;
        private String errorMessage;

        public Builder requestValueJson(NotificationQueueDto requestValueJson) {
            this.requestValueJson = requestValueJson;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public FeedbackResponseDto build() {
            return new FeedbackResponseDto(this);
        }
    }
}