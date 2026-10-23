package com.moneyfi.user.repository.admin;

public interface UserRequestsService {
    void changeUserSessionTime(Long adminUserId, String username, Integer time);
}
