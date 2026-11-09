package com.moneyfi.user.service.primary.general.inter.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moneyfi.constants.dto.NotificationQueueDto;
import com.moneyfi.user.model.primary.general.EmailNotificationFailedDetails;
import com.moneyfi.user.repository.primary.general.EmailNotificationFailedRepository;
import com.moneyfi.user.service.primary.general.inter.EmailNotificationFailedService;
import com.moneyfi.user.util.enums.UpdateType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static com.moneyfi.constants.constants.CommonConstants.*;
import static com.moneyfi.user.util.constants.StringConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationFailedServiceImpl implements EmailNotificationFailedService {

    @Value("${app.communication.queue-provider}")
    private String queueProvider;

    private final EmailNotificationFailedRepository emailNotificationFailedRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveFailedEmailNotificationDetails(NotificationQueueDto notificationQueueDto, String errorMessage, String failureStage) throws JsonProcessingException {
        LocalDateTime currentTime = LocalDateTime.now(ZoneId.systemDefault());
        JsonNode jsonNode = parseJson(notificationQueueDto);

        try {
            String mdcRequestId = getFieldValue(jsonNode, STRING_MDC_REQUEST_ID);
            EmailNotificationFailedDetails failedDetails = EmailNotificationFailedDetails.builder()
                    .mdcRequestId(mdcRequestId != null ? mdcRequestId : MDC.get(MDC_REQUEST_ID))
                    .message(errorMessage)
                    .valueJson(notificationQueueDto.getValueJson())
                    .username(getFieldValue(jsonNode, STRING_USERNAME))
                    .referenceNumber(getFieldValue(jsonNode, STRING_REFERENCE_NUMBER))
                    .name(getFieldValue(jsonNode, STRING_USER_NAME))
                    .failureStage(failureStage)
                    .messageType(notificationQueueDto.getNotificationQueueType())
                    .brokerUsed(StringUtils.capitalize(queueProvider))
                    .createdBy(UpdateType.AUTO.name())
                    .createdAt(currentTime)
                    .updatedBy(UpdateType.AUTO.name())
                    .updatedAt(currentTime)
                    .build();

            EmailNotificationFailedDetails saved =
                    emailNotificationFailedRepository.saveAndFlush(failedDetails);

            System.out.println("Saved entity = " + saved);
            System.out.println(
                    "Record count = "
                            + emailNotificationFailedRepository.count()
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveFailedEmailNotificationDetails(Long userId, String username, String errorMessage, String failureStage, String messageType) {
        LocalDateTime currentTime = LocalDateTime.now(ZoneId.systemDefault());
        EmailNotificationFailedDetails failedDetails = EmailNotificationFailedDetails.builder()
                .mdcRequestId(MDC.get(MDC_REQUEST_ID))
                .message(errorMessage)
                .username(username)
                .failureStage(failureStage)
                .messageType(messageType)
                .brokerUsed(StringUtils.capitalize(queueProvider))
                .createdBy(UpdateType.AUTO.name())
                .createdAt(currentTime)
                .updatedBy(UpdateType.AUTO.name())
                .updatedAt(currentTime)
                .build();
        emailNotificationFailedRepository.save(failedDetails);
    }

    private JsonNode parseJson(NotificationQueueDto dto) throws JsonProcessingException {
        return objectMapper.readTree(dto.getValueJson());
    }

    private static String getFieldValue(JsonNode node, String field) {
        JsonNode jsonNode = node.get(field);
        if (jsonNode == null || jsonNode.isNull()) {
            return null;
        }
        return jsonNode.asText();
    }
}
