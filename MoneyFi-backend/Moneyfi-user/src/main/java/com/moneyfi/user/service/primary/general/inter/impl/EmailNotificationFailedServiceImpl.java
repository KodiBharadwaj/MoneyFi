package com.moneyfi.user.service.primary.general.inter.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moneyfi.constants.dto.NotificationQueueDto;
import com.moneyfi.constants.dto.PaginatedRequestDto;
import com.moneyfi.user.model.primary.general.EmailNotificationFailedDetails;
import com.moneyfi.user.repository.primary.general.EmailNotificationFailedRepository;
import com.moneyfi.user.service.primary.admin.dto.response.FailedQueueDetailsResponseDto;
import com.moneyfi.user.service.primary.general.inter.EmailNotificationFailedService;
import com.moneyfi.user.util.enums.UpdateType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static com.moneyfi.constants.constants.CommonConstants.*;

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
        emailNotificationFailedRepository.save(failedDetails);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveFailedEmailNotificationDetails(String username, String errorMessage, String failureStage, String messageType) {
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

    @Override
    public List<FailedQueueDetailsResponseDto> getFailedQueueEmailNotificationDetails(OffsetDateTime startDate, OffsetDateTime endDate, PaginatedRequestDto requestDto) {
        int page = (int) (requestDto.getOffset() / requestDto.getLimit());
        Sort sort = Sort.by(
                Sort.Direction.fromString(Optional.ofNullable(requestDto.getSortOrder()).orElse("DESC")),
                Optional.ofNullable(requestDto.getSortBy()).orElse("createdAt")
        );

        Pageable pageable = PageRequest.of(page, requestDto.getLimit().intValue(), sort);

        Page<EmailNotificationFailedDetails> result =
                emailNotificationFailedRepository.getFailedQueueEmailNotificationDetails(startDate.toLocalDateTime(), endDate.toLocalDateTime(), requestDto.getSearch(), pageable);

        return result.getContent()
                .stream()
                .<FailedQueueDetailsResponseDto>map(entity -> FailedQueueDetailsResponseDto.builder()
                        .failedQueueId(entity.getId())
                        .mdcRequestId(entity.getMdcRequestId())
                        .referenceNumber(entity.getReferenceNumber())
                        .username(entity.getUsername())
                        .name(entity.getName())
                        .message(entity.getMessage())
                        .valueJson(entity.getValueJson())
                        .failureStage(entity.getFailureStage())
                        .messageType(entity.getMessageType())
                        .streamPlatform(entity.getBrokerUsed())
                        .failureTime(entity.getCreatedAt())
                        .totalCount(result.getTotalElements())
                        .build())
                .toList();
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
