package com.moneyfi.user.service.gmailsync.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
public class GmailConsentResponse {
    Integer count;
    Integer configuredCount;
    boolean consentStatus;
}
