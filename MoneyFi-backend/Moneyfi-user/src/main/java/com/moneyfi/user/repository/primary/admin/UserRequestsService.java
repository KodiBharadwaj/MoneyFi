package com.moneyfi.user.repository.primary.admin;

public interface UserRequestsService {
    void changeUserSessionTime(Long adminUserId, String username, Integer time);
}
