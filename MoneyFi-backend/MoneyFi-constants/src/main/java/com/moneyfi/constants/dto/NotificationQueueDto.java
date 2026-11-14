package com.moneyfi.constants.dto;

public class NotificationQueueDto {

    private String notificationQueueType;
    private String valueJson;

    public NotificationQueueDto() {
    }

    public NotificationQueueDto(String notificationQueueType, String valueJson) {
        this.notificationQueueType = notificationQueueType;
        this.valueJson = valueJson;
    }

    public String getNotificationQueueType() {
        return notificationQueueType;
    }

    public void setNotificationQueueType(String notificationQueueType) {
        this.notificationQueueType = notificationQueueType;
    }

    public String getValueJson() {
        return valueJson;
    }

    public void setValueJson(String valueJson) {
        this.valueJson = valueJson;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String notificationQueueType;
        private String valueJson;

        public Builder notificationQueueType(String notificationQueueType) {
            this.notificationQueueType = notificationQueueType;
            return this;
        }

        public Builder valueJson(String valueJson) {
            this.valueJson = valueJson;
            return this;
        }

        public NotificationQueueDto build() {
            return new NotificationQueueDto(
                    notificationQueueType,
                    valueJson
            );
        }
    }

    @Override
    public String toString() {
        return "NotificationQueueDto{" +
                "notificationQueueType='" + notificationQueueType + '\'' +
                ", valueJson='" + valueJson + '\'' +
                '}';
    }
}