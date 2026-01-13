package com.moneyfi.user.util.enums;

public enum TargetUsersForScheduleNotification {
    ALL("All"),
    SPECIFIC("Specific");

    private String targetUsers;

    TargetUsersForScheduleNotification(String targetUsers) {
        this.targetUsers = targetUsers;
    }

    public String getTargetUser() {
        return targetUsers;
    }
}
