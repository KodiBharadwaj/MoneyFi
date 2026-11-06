package com.moneyfi.user.service;

import com.moneyfi.user.exceptions.ResourceNotFoundException;
import com.moneyfi.user.model.secondary.SupportAgentWorkload;
import com.moneyfi.user.repository.secondary.SupportAgentWorkloadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static com.moneyfi.user.util.constants.StringConstants.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class PrimaryToSecondaryDbFlowImpl implements PrimaryToSecondaryDbFlow {

    @Value("${user.customer-care.conversations-count}")
    private int maxConcurrentConversations;

    private final SupportAgentWorkloadRepository supportAgentWorkloadRepository;

    @Async
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCustomerCareAgentIntoSecondaryDb(Long userId) {
        SupportAgentWorkload supportAgentWorkload = SupportAgentWorkload.builder()
                .agentId(userId)
                .online(Boolean.FALSE)
                .available(Boolean.FALSE)
                .activeConversationCount(0)
                .updatedAt(Instant.now())
                .maxConcurrentConversations(maxConcurrentConversations)
                .build();
        supportAgentWorkloadRepository.save(supportAgentWorkload);
    }

    @Async
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCustomerCareAvailability(Long userId, boolean activeStatus) {
        SupportAgentWorkload existingAgent = supportAgentWorkloadRepository.findByAgentId(userId).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        SupportAgentWorkload updatedAgent = existingAgent.toBuilder()
                .online(activeStatus)
                .available(activeStatus)
                .updatedAt(Instant.now())
                .build();
        supportAgentWorkloadRepository.save(updatedAgent);
    }
}
