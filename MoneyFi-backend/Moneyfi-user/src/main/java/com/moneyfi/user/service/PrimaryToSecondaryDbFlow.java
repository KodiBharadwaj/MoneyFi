package com.moneyfi.user.service;

public interface PrimaryToSecondaryDbFlow {
    void addCustomerCareAgentIntoSecondaryDb(Long userId);

    void updateCustomerCareAvailability(Long userId, boolean activeStatus);
}
