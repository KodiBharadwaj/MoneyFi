package com.moneyfi.user.service;

import com.moneyfi.user.model.secondary.SupportAgentWorkload;

public interface PrimaryToSecondaryDbFlow {
    void addCustomerCareAgentIntoSecondaryDb(Long userId);

    void updateCustomerCareAvailability(Long userId, boolean activeStatus);

    SupportAgentWorkload getAgentDetails(Long agentId);
}
