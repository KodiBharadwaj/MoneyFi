package com.moneyfi.apigateway.service.general;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.UserCredentials;
import com.moneyfi.apigateway.exceptions.CustomAuthenticationFailedException;
import com.moneyfi.apigateway.exceptions.ScenarioNotPossibleException;
import com.moneyfi.apigateway.model.gmailsync.GmailAuth;
import com.moneyfi.apigateway.repository.gmailsync.GmailSyncRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import static com.moneyfi.apigateway.util.constants.StringConstants.*;
import static com.moneyfi.apigateway.util.constants.StringUrls.*;

@Component
@RequiredArgsConstructor
public class GoogleOAuthEndPointDealerService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    private final RestTemplate externalRestTemplateForOAuth;
    private final CryptoUtil cryptoUtil;

    private static final String GMAIL_SYNC_NOT_ENABLED = "Gmail sync not enabled";
    private static final String GMAIL_ACCESS_TOKEN_FAILED = "Failed to obtain access token from Google";
    private static final String GMAIL_INVALID_SCOPE = "Invalid scope. Please try again";
    private static final String GMAIL_PERMISSION_DENIED_MESSAGE = "Gmail permission not granted. Please re-consent again";
    private static final String GOOGLE_USER_INFO_FETCH_FAILED = "Failed to fetch Google user info";


    public Map<String, Object> exchangeAuthorizationCodeAndGetAccessRefreshTokens(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add(CLIENT_ID, googleClientId);
        form.add(CLIENT_SECRET, googleClientSecret);
        form.add(CODE, code);
        form.add(GRANT_TYPE, AUTHORIZATION_CODE);
        form.add(REDIRECT_URI, POST_MESSAGE);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);
        Map<String, Object> tokenResponse = externalRestTemplateForOAuth.postForObject(GOOGLE_TOKEN_END_POINT_URL, request, Map.class);

        if (tokenResponse == null || !tokenResponse.containsKey(ACCESS_TOKEN)) {
            throw new CustomAuthenticationFailedException(GMAIL_ACCESS_TOKEN_FAILED);
        }
        return tokenResponse;
    }

    public void securityValidationCheckToVerifyToken(String type, String accessToken) {
        Map<String, Object> tokenInfo = externalRestTemplateForOAuth.getForObject(GOOGLE_TOKEN_EXTRA_SECURITY_CHECK_URL, Map.class, accessToken);

        String scope = tokenInfo != null ? (String) tokenInfo.get(SCOPE) : null;
        if (scope == null) {
            throw new ScenarioNotPossibleException(GMAIL_INVALID_SCOPE);
        } else {
            if (GMAIL_SYNC.equalsIgnoreCase(type) && !scope.contains(GOOGLE_GMAIL_READONLY_CHECK_URL)) {
                throw new CustomAuthenticationFailedException(GMAIL_PERMISSION_DENIED_MESSAGE);
            }
        }
    }

    public Map<String, Object> getUserInformationFromAccessToken(String accessToken) {
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(accessToken);
        HttpEntity<Void> authRequest = new HttpEntity<>(authHeaders);
        Map<String, Object> userInfo = externalRestTemplateForOAuth.exchange(GOOGLE_USER_INFO_GET_URL, HttpMethod.GET, authRequest, Map.class).getBody();

        if (userInfo == null || userInfo.get(STRING_EMAIL) == null) {
            throw new CustomAuthenticationFailedException(GOOGLE_USER_INFO_FETCH_FAILED);
        }
        return userInfo;
    }

    public Gmail gmailClientForUser(GmailSyncRepository gmailSyncRepository, Long userId) throws IOException, URISyntaxException {
        GmailAuth auth = gmailSyncRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException(GMAIL_SYNC_NOT_ENABLED));

        UserCredentials credentials = UserCredentials.newBuilder().setClientId(googleClientId)
                .setClientSecret(googleClientSecret)
                .setAccessToken(new AccessToken(cryptoUtil.decrypt(auth.getAccessToken()), Date.from(auth.getExpiresAt())))
                .setRefreshToken(cryptoUtil.decrypt(auth.getRefreshToken()))
                .setTokenServerUri(new URI(GOOGLE_TOKEN_END_POINT_URL))
                .build();
        if (credentials.getAccessToken().getExpirationTime().before(new Date())) {
            credentials.refresh();
            AccessToken refreshed = credentials.getAccessToken();
            auth.setAccessToken(cryptoUtil.encrypt(refreshed.getTokenValue()));
            auth.setExpiresAt(refreshed.getExpirationTime().toInstant());
            gmailSyncRepository.save(auth);
        }
        return new Gmail.Builder(
                new NetHttpTransport(), JacksonFactory.getDefaultInstance(), new HttpCredentialsAdapter(credentials)
        ).setApplicationName(MONEYFI_APPLICATION_NAME).build();
    }

    public void setGmailAuthDetails(GmailAuth auth, Long userId, String accessToken, String refreshToken, Number expiresIn) {
        auth.setUserId(userId);
        auth.setAccessToken(cryptoUtil.encrypt(accessToken));
        if (refreshToken != null) {
            auth.setRefreshToken(cryptoUtil.encrypt(refreshToken));
        }
        auth.setExpiresAt(Instant.now().plusSeconds(expiresIn.longValue()));
        auth.setIsActive(Boolean.TRUE);
    }
}
