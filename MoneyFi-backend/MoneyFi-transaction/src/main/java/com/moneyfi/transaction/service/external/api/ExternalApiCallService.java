package com.moneyfi.transaction.service.external.api;

import com.moneyfi.constants.dto.BatchInfoForEmailDto;
import com.moneyfi.transaction.service.webclient.WebClientService;
import com.moneyfi.transaction.utils.constants.StringUrls;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalApiCallService {

    private final WebClientService webClientService;

    public void externalCallToUserServiceToSendPdf(byte[] pdfBytes, String authHeader) {
        String token = authHeader.substring(7);
        webClientService.exchange(
                HttpMethod.POST,
                StringUrls.ACCOUNT_STATEMENT_USER_SERVICE_URL,
                null,
                pdfBytes,
                token,
                new ParameterizedTypeReference<Void>() {}
        );
    }
    /**
      * Rest Template call
        private void apiCallToGatewayServiceToSendEmail(byte[] pdfBytes, String authHeader) {
            String token = authHeader.substring(7);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setBearerAuth(token);
            HttpEntity<byte[]> requestEntity = new HttpEntity<>(pdfBytes, headers);

            ResponseEntity<Void> response = restTemplate.exchange(
                    StringConstants.ACCOUNT_STATEMENT_USER_SERVICE_URL,
                    HttpMethod.POST,
                    requestEntity,
                    Void.class
            );
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ResourceNotFoundException("Failed to send email: " + response.getStatusCode());
            }
        }
     */

    public void externalCallToUserServiceToSendEmailToUser(List<BatchInfoForEmailDto> batchInfoForEmailDto, String token) {
        webClientService.exchange(
                HttpMethod.POST,
                StringUrls.BATCH_INFO_EMAIL_USER_SERVICE_URL,
                null,
                batchInfoForEmailDto,
                token,
                new ParameterizedTypeReference<Void>() {}
        );
    }
}
