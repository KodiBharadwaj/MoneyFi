package com.moneyfi.user.service.general.inter.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moneyfi.user.model.general.EmailNotificationFailedDetails;
import com.moneyfi.user.repository.general.EmailNotificationFailedRepository;
import com.moneyfi.user.service.general.inter.EmailNotificationFailedDetailsDto;
import com.moneyfi.user.service.general.inter.EmailNotificationFailedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationFailedServiceImpl implements EmailNotificationFailedService {

    private final EmailNotificationFailedRepository emailNotificationFailedRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveFailedEmailNotificationDetails(String message) throws JsonProcessingException {
        EmailNotificationFailedDetailsDto dto = objectMapper.readValue(message, EmailNotificationFailedDetailsDto.class);
        log.info("validating incoming request: {}", dto);

        LocalDateTime currentTime = LocalDateTime.now(ZoneId.systemDefault());
        EmailNotificationFailedDetails failedDetails = new EmailNotificationFailedDetails();
        failedDetails.setMdcRequestId(dto.getMdcRequestId());
        failedDetails.setReferenceNumber(dto.getReferenceNumber());
        failedDetails.setMessage(dto.getMessage());
        failedDetails.setUsername(dto.getUsername());
        failedDetails.setUserId(dto.getUserId());
        failedDetails.setName(dto.getName());
        failedDetails.setCreatedBy("Auto");
        failedDetails.setCreatedAt(currentTime);
        failedDetails.setUpdatedBy("Auto");
        failedDetails.setUpdatedAt(currentTime);
        emailNotificationFailedRepository.save(failedDetails);
    }
}
