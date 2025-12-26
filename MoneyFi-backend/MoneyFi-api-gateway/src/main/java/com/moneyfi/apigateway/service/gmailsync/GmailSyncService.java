package com.moneyfi.apigateway.service.gmailsync;

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
import com.moneyfi.apigateway.exceptions.ScenarioNotPossibleException;
import com.moneyfi.apigateway.model.gmailsync.GmailAuth;
import com.moneyfi.apigateway.model.gmailsync.GmailProcessedMessageEntity;
import com.moneyfi.apigateway.repository.gmailsync.GmailSyncRepository;
import com.moneyfi.apigateway.repository.gmailsync.GmailProcessedMessageRepository;
import com.moneyfi.apigateway.util.CryptoUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(rollbackOn = Exception.class)
public class GmailSyncService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    private final RestTemplate externalRestTemplateForOAuth;
    private final GmailSyncRepository gmailAuthRepository;
    private final CryptoUtil cryptoUtil;
    private final GmailProcessedMessageRepository processedRepo;

    public List<ParsedTransaction> enableSync(String code, Long userId) throws IOException {
        if(isSyncEnabled(userId)) throw new ScenarioNotPossibleException("User has no access to sync. Please try tomorrow");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", googleClientId);
        form.add("client_secret", googleClientSecret);
        form.add("code", code);
        form.add("grant_type", "authorization_code");
        form.add("redirect_uri", "postmessage");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);
        Map<String, Object> tokenResponse = externalRestTemplateForOAuth.postForObject("https://oauth2.googleapis.com/token", request, Map.class);

        GmailAuth auth = new GmailAuth();
        auth.setUserId(userId);
        auth.setAccessToken(cryptoUtil.encrypt((String) tokenResponse.get("access_token")));
        auth.setRefreshToken(cryptoUtil.encrypt((String) tokenResponse.get("refresh_token")));
        auth.setExpiresAt(Instant.now().plusSeconds(((Number) tokenResponse.get("expires_in")).longValue()));
        gmailAuthRepository.save(auth);

        return syncEmails(userId);
    }

    public boolean isSyncEnabled(Long userId) {
        return gmailAuthRepository.existsByUserId(userId);
    }

    public List<ParsedTransaction> syncEmails(Long userId) throws IOException {
        Gmail gmail = gmailClientForUser(userId);

        /** String query =
                "from:(@hdfcbank.net OR @icicibank.com OR @sbi.co.in OR @microsoftonline.com OR @gmail.com) " +
                        "subject:(debited OR credited OR UPI OR MoneyFi - user requests) newer_than:7d"; **/

        String query = "in:anywhere newer_than:60d";
        ListMessagesResponse response = gmail.users().messages().list("me")
                        .setQ(query)
                        .setIncludeSpamTrash(false)
                        .setMaxResults(10L)
                        .execute();
        if (response.getMessages() == null) return null;

        List<ParsedTransaction> responseList = new ArrayList<>();
        for (Message msg : response.getMessages()) {
            if (alreadyProcessed(msg.getId(), userId)) continue;

            Message fullMessage = gmail.users().messages().get("me", msg.getId()).execute();
            ParsedTransaction parsedTransaction = parseTransaction(fullMessage);
            if(parsedTransaction != null && parsedTransaction.getCategory().equalsIgnoreCase("Others")
                    && parsedTransaction.getDescription().equalsIgnoreCase("Bank transaction") && parsedTransaction.getTransactionType().equalsIgnoreCase("Unrecognized")) continue;
            responseList.add(parsedTransaction);
            markProcessed(msg.getId(), userId);
        }
        return responseList;
    }

    private Gmail gmailClientForUser(Long userId) throws IOException {
        GmailAuth auth = gmailAuthRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("Gmail sync not enabled for user"));

        String accessToken = cryptoUtil.decrypt(auth.getAccessToken());
        AccessToken token = new AccessToken(accessToken, Date.from(auth.getExpiresAt()));
        GoogleCredentials credentials = GoogleCredentials.create(token).createScoped(Collections.singleton(GmailScopes.GMAIL_READONLY));
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
        String rawBody = extractEmailBody(message);
        log.info("Checking rawBody {} ", rawBody);

        if (rawBody.isBlank()) return null;
        String body = normalizeBody(rawBody);
        if (!containsTransactionKeywords(body)) return null;
        return new ParsedTransaction(detectTransactionCategory(body), detectTransactionDescription(body),
                detectTransactionAmount(body), detectTransactionType(body), detectTransactionDateTime(message));
    }

    private String normalizeBody(String body) {
        return body
                .replaceAll("<[^>]*>", " ")
                .replaceAll("&nbsp;", " ")
                .replaceAll("[₹]", " rs ")
                .replaceAll("\\s+", " ")
                .toLowerCase()
                .trim();
    }

    private boolean containsTransactionKeywords(String body) {
        return body.contains("debited")
                || body.contains("credited")
                || body.contains("credit")
                || body.contains("debit")
                || body.contains("spent")
                || body.contains("received")
                || body.contains("withdrawn")
                || body.contains("payment")
                || body.contains("transferred")
                || body.contains("transfer")
                || body.contains("transaction")
                || body.contains("money")
                || body.contains("rupees")
                || body.contains("amount")
                || body.contains("purchased")
                || body.contains("purchase");
    }

    private String detectTransactionType(String body) {
        boolean credit =
                body.contains("credited") || body.contains("credit") ||
                body.contains("received") ||
                body.contains("refund");

        boolean debit =
                body.contains("debited") || body.contains("debit") ||
                body.contains("spent") ||
                body.contains("withdrawn") ||
                body.contains("purchase") || body.contains("purchased") ||
                body.contains("transferred") || body.contains("transfer") ||
                body.contains("paid");
        boolean creditOrDebit =
                body.contains("transaction") || body.contains("payment");

        if (credit && !debit) return "CREDIT";
        if (debit) return "DEBIT";
        if (creditOrDebit) return "CREDIT OR DEBIT";
        return "Unrecognized";
    }

    private BigDecimal detectTransactionAmount(String body) {
        Matcher matcher = Pattern.compile("(debited|credited|spent|received|withdrawn)[^\\d]{0,20}" + "(rs\\.?|inr)?\\s*([0-9,]+(\\.\\d{1,2})?)").matcher(body);
        if (matcher.find()) {
            return new BigDecimal(matcher.group(3).replace(",", ""));
        }

        Matcher fallbackMatcher = Pattern.compile("(rs\\.?|inr)\\s*([0-9,]+(\\.\\d{1,2})?)").matcher(body);
        if (fallbackMatcher.find()) {
            return new BigDecimal(fallbackMatcher.group(2).replace(",", ""));
        }
        return null;
    }

    private LocalDateTime detectTransactionDateTime(Message message) {
        if (message.getInternalDate() != null) {
            return Instant.ofEpochMilli(message.getInternalDate())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
        }
        return LocalDateTime.now();
    }

    private String detectTransactionDescription(String body) {
        if (body.contains("upi")) return "UPI transaction";
        if (body.contains("card")) {
            if (body.contains("credit")) return "Credit Card transaction";
            else if (body.contains("debit")) return "Debit Card transaction";
        }
        if (body.contains("atm")) return "ATM withdrawal";
        if (body.contains("cash")) return "Cash transaction";
        return "Bank transaction";
    }

    private String detectTransactionCategory(String body) {
        if (body.contains("swiggy") || body.contains("zomato") || body.contains("bigbasket") || body.contains("jiomart") || body.contains("dineout"))
            return "Food";
        if (body.contains("amazon") || body.contains("flipkart"))
            return "Shopping";
        if (body.contains("uber") || body.contains("rapido") || body.contains("train") || body.contains("flight") || body.contains("bus") || body.contains("ticket"))
            return "Travelling";
        if (body.contains("bill") || body.contains("electricity") || body.contains("water") || body.contains("wifi"))
            return "Bills & utilities";
        if (body.contains("rent"))
            return "House Rent";
        if (body.contains("salary"))
            return "Salary";
        if (body.contains("investments") || body.contains("stocks") || body.contains("returns"))
            return "Investments";
        if (body.contains("business") || body.contains("deal"))
            return "Business";
        return "Others";
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
        if (part.getBody() != null && part.getBody().getData() != null) {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(part.getBody().getData());
            return new String(decodedBytes, StandardCharsets.UTF_8);
        }

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
