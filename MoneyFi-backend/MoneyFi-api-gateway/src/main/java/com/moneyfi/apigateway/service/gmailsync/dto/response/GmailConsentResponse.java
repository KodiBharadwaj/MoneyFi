package com.moneyfi.apigateway.service.gmailsync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GmailConsentResponse<T> {
    T data;
    boolean consentStatus;
}
