package com.moneyfi.apigateway.service.userservice;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.moneyfi.apigateway.dto.ParsedTransaction;
import com.moneyfi.apigateway.model.common.GmailAuth;
import com.moneyfi.apigateway.model.common.GmailProcessedMessageEntity;
import com.moneyfi.apigateway.repository.user.GmailSyncRepository;
import com.moneyfi.apigateway.repository.user.auth.GmailProcessedMessageRepository;
import com.moneyfi.apigateway.util.CryptoUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional
public class GmailSyncService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    private final RestTemplate externalRestTemplateForOAuth;
    private final GmailSyncRepository gmailAuthRepository;
    private final CryptoUtil cryptoUtil;
    private final GmailProcessedMessageRepository processedRepo;

    public void enableSync(String code, Long userId) throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 1️⃣ Exchange code → tokens
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", googleClientId);
        form.add("client_secret", googleClientSecret);
        form.add("code", code);
        form.add("grant_type", "authorization_code");
        form.add("redirect_uri", "postmessage");

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(form, headers);


        Map<String, Object> tokenResponse =
                externalRestTemplateForOAuth
                        .postForObject("https://oauth2.googleapis.com/token", request, Map.class);

        // 2️⃣ Save tokens
        GmailAuth auth = new GmailAuth();
        auth.setUserId(userId);
        auth.setAccessToken(cryptoUtil.encrypt((String) tokenResponse.get("access_token")));
        auth.setRefreshToken(cryptoUtil.encrypt((String) tokenResponse.get("refresh_token")));
        auth.setExpiresAt(
                Instant.now().plusSeconds(
                        ((Number) tokenResponse.get("expires_in")).longValue()
                )
        );

        gmailAuthRepository.save(auth);

        // 3️⃣ Initial sync (last 7 days)
        syncEmails(userId);
    }

    public boolean isSyncEnabled(Long userId) {
        return gmailAuthRepository.existsByUserId(userId);
    }

    public void syncEmails(Long userId) throws IOException {

        Gmail gmail = gmailClientForUser(userId);

        String query =
                "from:(@hdfcbank.net OR @icicibank.com OR @sbi.co.in) " +
                        "subject:(debited OR credited OR UPI) newer_than:7d";

        ListMessagesResponse response =
                gmail.users().messages().list("me")
                        .setQ(query)
                        .setMaxResults(20L)
                        .execute();

        if (response.getMessages() == null) return;

        for (Message msg : response.getMessages()) {
            if (alreadyProcessed(msg.getId(), userId)) continue;

            Message fullMessage =
                    gmail.users().messages().get("me", msg.getId()).execute();

            ParsedTransaction tx = parseTransaction(fullMessage);
            System.out.println("checking value: " + tx);
            markProcessed(msg.getId(), userId);
        }
    }

    private Gmail gmailClientForUser(Long userId) throws IOException {

        GmailAuth auth = gmailAuthRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Gmail sync not enabled for user"));

        String accessToken = cryptoUtil.decrypt(auth.getAccessToken());

        AccessToken token = new AccessToken(
                accessToken,
                Date.from(auth.getExpiresAt())
        );

        GoogleCredentials credentials =
                GoogleCredentials.create(token)
                        .createScoped(Collections.singleton(GmailScopes.GMAIL_READONLY));

        credentials.refreshIfExpired();

        return new Gmail.Builder(
                new NetHttpTransport(),
                JacksonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
        ).setApplicationName("MoneyFi").build();
    }

    private boolean alreadyProcessed(String messageId, Long userId) {
        return processedRepo.existsByMessageIdAndUserId(messageId, userId);
    }

    private void markProcessed(String messageId, Long userId) {
        GmailProcessedMessageEntity entity = new GmailProcessedMessageEntity();
        entity.setMessageId(messageId);
        entity.setUserId(userId);
        processedRepo.save(entity);
    }

    private ParsedTransaction parseTransaction(Message message) {

        String body = extractEmailBody(message);

        // Example regex (simple, improve later)
        Pattern amountPattern = Pattern.compile("Rs\\.?\\s?(\\d+[,.]?\\d*)");
        Matcher matcher = amountPattern.matcher(body);

        if (!matcher.find()) return null;

        BigDecimal amount =
                new BigDecimal(matcher.group(1).replace(",", ""));

        String type =
                body.toLowerCase().contains("credited")
                        ? "CREDIT"
                        : "DEBIT";

        ParsedTransaction tx = new ParsedTransaction();
        tx.setAmount(amount);
        tx.setType(type);
        tx.setDescription("Imported from email");
        tx.setTransactionDate(LocalDateTime.now());

        return tx;
    }

    private String extractEmailBody(Message message) {
        try {
            if (message.getPayload() == null) {
                return "";
            }
            return getTextFromPayload(message.getPayload());
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract email body", e);
        }
    }

    private String getTextFromPayload(MessagePart part) throws Exception {

        // If the part has body data
        if (part.getBody() != null && part.getBody().getData() != null) {
            byte[] decodedBytes =
                    Base64.getUrlDecoder().decode(part.getBody().getData());

            return new String(decodedBytes, StandardCharsets.UTF_8);
        }

        // If multipart, recursively read parts
        if (part.getParts() != null) {
            for (MessagePart subPart : part.getParts()) {
                String result = getTextFromPayload(subPart);
                if (result != null && !result.isEmpty()) {
                    return result;
                }
            }
        }

        return "";
    }
}
