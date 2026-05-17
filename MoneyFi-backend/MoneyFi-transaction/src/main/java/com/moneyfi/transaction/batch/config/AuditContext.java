package com.moneyfi.transaction.batch.config;

public class AuditContext {

    private static final ThreadLocal<Long> CURRENT_USER = new ThreadLocal<>();

    public static void setCurrentUser(Long userId) {
        CURRENT_USER.set(userId);
    }

    public static Long getCurrentUser() {
        return CURRENT_USER.get();
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
