package com.moneyfi.wealthcore.service.api;

import com.moneyfi.constants.dto.GoalExpenseRelationRequestDto;
import com.moneyfi.wealthcore.service.webclient.WebClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.moneyfi.constants.constants.BasePathConstants.TRANSACTION_SERVICE_BASE_PATH;
import static com.moneyfi.constants.constants.BasePathConstants.USER_SERVICE_BASE_PATH;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalApiCallService {

    @Value("${service.urls.user-service}")
    private String userServiceUrl;

    @Value("${service.urls.transaction-service}")
    private String transactionServiceUrl;

    private final WebClientService webClientService;

    public List<Object[]> externalApiCallToTransactionService(String authHeader, Map<String, String> queryParams, String continuationUrl) {
        String token = authHeader.substring(7);
        return webClientService.exchange(
                HttpMethod.GET,
                transactionServiceUrl + TRANSACTION_SERVICE_BASE_PATH + continuationUrl,
                queryParams,
                null,
                token,
                new ParameterizedTypeReference<List<Object[]>>() {}
        );
    }

    public void externalApiCallToUserServiceToSendPdfEmail(byte[] pdfBytes, String authHeader, String continuationUrl) {
        String token = authHeader.substring(7);
        webClientService.exchange(
                HttpMethod.POST,
                userServiceUrl + USER_SERVICE_BASE_PATH + continuationUrl,
                null,
                pdfBytes,
                token,
                new ParameterizedTypeReference<Void>() {}
        );
    }

    public void externalApiCallToTransactionServiceToSaveExpense(String token, String continuationUrl, GoalExpenseRelationRequestDto goalExpenseRelationRequestDto) {
        webClientService.exchange(
                HttpMethod.POST,
                transactionServiceUrl + TRANSACTION_SERVICE_BASE_PATH + continuationUrl,
                null,
                goalExpenseRelationRequestDto,
                token,
                new ParameterizedTypeReference<Void>() {}
        );
    }

    public void externalApiCallToTransactionServiceToDeleteExpense(String token, String continuationUrl, List<Long> expenseIdsList) {
        webClientService.exchange(
                HttpMethod.DELETE,
                transactionServiceUrl + TRANSACTION_SERVICE_BASE_PATH + continuationUrl,
                null,
                expenseIdsList,
                token,
                new ParameterizedTypeReference<Void>() {}
        );
    }
}
