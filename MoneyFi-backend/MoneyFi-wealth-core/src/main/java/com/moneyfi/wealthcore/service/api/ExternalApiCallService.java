package com.moneyfi.wealthcore.service.api;

import com.moneyfi.constants.dto.GoalExpenseRelationRequestDto;
import com.moneyfi.wealthcore.service.webclient.WebClientService;
import com.moneyfi.wealthcore.utils.constants.StringUrls;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalApiCallService {

    private final WebClientService webClientService;

    public List<Object[]> externalApiCallToTransactionService(String authHeader, Map<String, String> queryParams, String continuationUrl) {
        String token = authHeader.substring(7);
        return webClientService.exchange(
                HttpMethod.GET,
                StringUrls.EUREKA_TRANSACTION_SERVICE_URL + continuationUrl,
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
                StringUrls.USER_SERVICE_URL_CONTROLLER + continuationUrl,
                null,
                pdfBytes,
                token,
                new ParameterizedTypeReference<Void>() {}
        );
    }

    public void externalApiCallToTransactionServiceToSaveExpense(String token, String continuationUrl, GoalExpenseRelationRequestDto goalExpenseRelationRequestDto) {
        webClientService.exchange(
                HttpMethod.POST,
                StringUrls.EUREKA_TRANSACTION_SERVICE_URL + continuationUrl,
                null,
                goalExpenseRelationRequestDto,
                token,
                new ParameterizedTypeReference<Void>() {}
        );
    }

    public void externalApiCallToTransactionServiceToDeleteExpense(String token, String continuationUrl, List<Long> expenseIdsList) {
        webClientService.exchange(
                HttpMethod.DELETE,
                StringUrls.EUREKA_TRANSACTION_SERVICE_URL + continuationUrl,
                null,
                expenseIdsList,
                token,
                new ParameterizedTypeReference<Void>() {}
        );
    }
}
