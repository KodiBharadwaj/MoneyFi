package com.moneyfi.apigateway.service.gmailsync;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.UserCredentials;
import com.moneyfi.apigateway.dto.ParsedTransaction;
import com.moneyfi.apigateway.exceptions.CustomAuthenticationFailedException;
import com.moneyfi.apigateway.exceptions.ScenarioNotPossibleException;
import com.moneyfi.apigateway.model.gmailsync.GmailAuth;
import com.moneyfi.apigateway.model.gmailsync.GmailProcessedMessageEntity;
import com.moneyfi.apigateway.model.gmailsync.GmailSyncHistory;
import com.moneyfi.apigateway.repository.common.CommonServiceRepository;
import com.moneyfi.apigateway.repository.gmailsync.GmailSyncHistoryRepository;
import com.moneyfi.apigateway.repository.gmailsync.GmailSyncRepository;
import com.moneyfi.apigateway.repository.gmailsync.GmailProcessedMessageRepository;
import com.moneyfi.apigateway.service.gmailsync.dto.response.GmailSyncHistoryResponse;
import com.moneyfi.apigateway.util.CryptoUtil;
import com.moneyfi.apigateway.util.enums.TransactionServiceType;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    private final GmailSyncRepository gmailSyncRepository;
    private final CryptoUtil cryptoUtil;
    private final GmailProcessedMessageRepository processedRepository;
    private final CommonServiceRepository commonServiceRepository;
    private final GmailSyncHistoryRepository gmailSyncHistoryRepository;

    public void enableSync(String code, String username, Long userId) {
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

        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth((String) tokenResponse.get("access_token"));
        HttpEntity<Void> authRequest = new HttpEntity<>(authHeaders);
        Map<String, Object> userInfo = externalRestTemplateForOAuth.exchange("https://openidconnect.googleapis.com/v1/userinfo", HttpMethod.GET, authRequest, Map.class).getBody();

        String gmailFromCode = (String) userInfo.get("email");
        if(!username.trim().equals(gmailFromCode.trim())) throw new CustomAuthenticationFailedException("Unauthorized Access");

        GmailAuth newAuth = new GmailAuth();
        Optional<GmailAuth> gmailAuth = gmailSyncRepository.findByUserId(userId);
        if(gmailAuth.isPresent()) newAuth = gmailAuth.get();
        newAuth.setUserId(userId);
        newAuth.setAccessToken(cryptoUtil.encrypt((String) tokenResponse.get("access_token")));
        newAuth.setRefreshToken(cryptoUtil.encrypt((String) tokenResponse.get("refresh_token")));
        newAuth.setExpiresAt(Instant.now().plusSeconds(((Number) tokenResponse.get("expires_in")).longValue()));
        gmailSyncRepository.save(newAuth);
    }

    public GmailAuth isSyncEnabled(Long userId) {
        return gmailSyncRepository.existsByUserId(userId).orElse(null);
    }

    public Integer getGmailConsentStatus(Long userId) {
        GmailAuth gmailAuth = isSyncEnabled(userId);
        return gmailAuth != null ? gmailAuth.getCount() != null ? gmailAuth.getCount() : 0 : null;
    }

    public List<GmailSyncHistoryResponse> getSyncHistoryResponse(Long userId) {
        return gmailSyncHistoryRepository.findByUserId(userId)
                .stream()
                .collect(Collectors.groupingBy(history -> history.getSyncTime().toLocalDate()))
                .entrySet()
                .stream()
                .map(entry -> {
                    LocalDateTime latestSyncTime = entry.getValue().stream()
                            .map(GmailSyncHistory::getSyncTime)
                            .max(LocalDateTime::compareTo)
                            .orElse(null);
                    return GmailSyncHistoryResponse.builder()
                            .syncTime(latestSyncTime)
                            .syncCount(entry.getValue().size())
                            .build();
                })
                .sorted(Comparator.comparing(GmailSyncHistoryResponse::getSyncTime).reversed())
                .toList();
    }

    public Map<Integer, List<ParsedTransaction>> startGmailSync(Long userId, LocalDate date) throws IOException, URISyntaxException {
        GmailAuth gmailAuth = isSyncEnabled(userId);
        if(gmailAuth != null && gmailAuth.getCount() >= 3) throw new ScenarioNotPossibleException("Sync limit crossed. Please login again to use");
        else if(gmailAuth == null) gmailAuth = new GmailAuth();

        gmailAuth.setCount((gmailAuth.getCount() != null ? gmailAuth.getCount() : 0) + 1);
        gmailSyncRepository.save(gmailAuth);
        /** return new HashMap<>(Map.of(2, List.of(new ParsedTransaction(1, "Upi", new BigDecimal("123"), "CREDIT OR DEBIT", LocalDateTime.now())))); **/
        return Map.of(3 - gmailAuth.getCount(), syncEmails(userId, date).stream().filter(Objects::nonNull).toList());
    }

    private List<ParsedTransaction> syncEmails(Long userId, LocalDate date) throws IOException, URISyntaxException {
        Gmail gmail = gmailClientForUser(userId);
        /** String query =
                "from:(@hdfcbank.net OR @icicibank.com OR @sbi.co.in OR @microsoftonline.com OR @gmail.com) " +
                        "subject:(debited OR credited OR UPI OR MoneyFi - user requests) newer_than:7d"; **/
        long start = date
                .atStartOfDay(ZoneOffset.UTC)
                .toEpochSecond();
        long end = date
                .plusDays(1)
                .atStartOfDay(ZoneOffset.UTC)
                .toEpochSecond();

        String query = "after:" + start + " before:" + end;
        ListMessagesResponse response = gmail.users().messages().list("me")
                        .setQ(query)
                        .setIncludeSpamTrash(false)
                        .setMaxResults(100L)
                        .execute();
        gmailSyncHistoryRepository.save(new GmailSyncHistory(date.atTime(LocalTime.now()), userId));
        if (response.getMessages() == null) return new ArrayList<>();

        List<String> categories = commonServiceRepository.getCategoriesBasedOnTransactionType(TransactionServiceType.EXPENSE.name());
        Map<String, Integer> categoryNameIdMap = new HashMap<>();
        categories.forEach(category -> {
            String categoryName = category.split("-")[0];
            Integer categoryId = Integer.parseInt(category.split("-")[1]);
            categoryNameIdMap.put(categoryName, categoryId);
        });
        List<ParsedTransaction> responseList = new ArrayList<>();
        for (Message msg : response.getMessages()) {
            if (alreadyProcessed(msg.getId(), userId)) continue;
            Message fullMessage = gmail.users().messages().get("me", msg.getId()).execute();
            try {
                ParsedTransaction parsedTransaction = parseTransaction(fullMessage, categoryNameIdMap);
                if(parsedTransaction == null) continue;
                if(Objects.equals(parsedTransaction.getCategoryId(), categoryNameIdMap.get("Other"))
                        && parsedTransaction.getDescription().equalsIgnoreCase("Bank transaction") && parsedTransaction.getTransactionType().equalsIgnoreCase("Unrecognized")) continue;
                parsedTransaction.setGmailProcessedId(markProcessed(msg.getId(), userId));
                responseList.add(parsedTransaction);
            } catch (Exception ex) {
                log.warn(
                        "Skipping email {} for user {} due to parse error: {}",
                        msg.getId(),
                        userId,
                        ex.getMessage()
                );
            }
        }
        return responseList;
    }

    private Gmail gmailClientForUser(Long userId) throws IOException, URISyntaxException {
        GmailAuth auth = gmailSyncRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("Gmail sync not enabled"));

        UserCredentials credentials = UserCredentials.newBuilder().setClientId(googleClientId)
                        .setClientSecret(googleClientSecret)
                        .setAccessToken(new AccessToken(cryptoUtil.decrypt(auth.getAccessToken()), Date.from(auth.getExpiresAt())))
                        .setRefreshToken(cryptoUtil.decrypt(auth.getRefreshToken()))
                        .setTokenServerUri(new URI("https://oauth2.googleapis.com/token"))
                        .build();
        if (credentials.getAccessToken().getExpirationTime().before(new Date())) {
            credentials.refresh();
            AccessToken refreshed = credentials.getAccessToken();
            auth.setAccessToken(cryptoUtil.encrypt(refreshed.getTokenValue()));
            auth.setExpiresAt(refreshed.getExpirationTime().toInstant());
            gmailSyncRepository.save(auth);
        }
        return new Gmail.Builder(
                new NetHttpTransport(),
                JacksonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
        ).setApplicationName("MoneyFi").build();
    }

    private boolean alreadyProcessed(String messageId, Long userId) {
        return processedRepository.findByMessageIdAndUserId(messageId, userId).map(GmailProcessedMessageEntity::isVerified).orElse(false);
    }

    private Long markProcessed(String messageId, Long userId) {
        Optional<GmailProcessedMessageEntity> entity = processedRepository.findByMessageId(messageId);
        if(entity.isPresent()) {
            entity.get().setUpdatedAt(LocalDateTime.now());
            return processedRepository.save(entity.get()).getId();
        }
        GmailProcessedMessageEntity newEntity = new GmailProcessedMessageEntity();
        newEntity.setMessageId(messageId);
        newEntity.setUserId(userId);
        return processedRepository.save(newEntity).getId();
    }

    private ParsedTransaction parseTransaction(Message message, Map<String, Integer> categoryNameIdMap) {
        String rawBody = extractEmailBody(message);
        log.info("Checking rawBody {} ", rawBody);
        if (rawBody.isBlank()) return null;
        String body = normalizeBody(rawBody);
        if (!containsTransactionKeywords(body)) return null;
        return new ParsedTransaction(detectTransactionCategory(body, categoryNameIdMap), null, detectTransactionDescription(body),
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
                body.contains("received") || body.contains("settlement") ||
                body.contains("refund");

        boolean debit =
                body.contains("debited") || body.contains("debit") ||
                body.contains("spent") || body.contains("settlement") ||
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
        if (body.contains("bank")) return "Bank Transaction";
        return "Bank transaction";
    }

    private Integer detectTransactionCategory(String body, Map<String, Integer> categoryNameIdMap) {
        if (body.contains("swiggy") || body.contains("zomato") || body.contains("bigbasket") || body.contains("jiomart") || body.contains("dineout"))
            return categoryNameIdMap.get("Food");
        if (body.contains("amazon") || body.contains("flipkart"))
            return categoryNameIdMap.get("Shopping");
        if (body.contains("uber") || body.contains("rapido") || body.contains("train") || body.contains("flight") || body.contains("bus") || body.contains("ticket"))
            return categoryNameIdMap.get("Travelling");
        if (body.contains("bill") || body.contains("electricity") || body.contains("water") || body.contains("wifi"))
            return categoryNameIdMap.get("Bills & utilities");
        if (body.contains("rent"))
            return categoryNameIdMap.get("House Rent");
        if (body.contains("salary"))
            return categoryNameIdMap.get("Salary");
        if (body.contains("investments") || body.contains("stocks") || body.contains("returns"))
            return categoryNameIdMap.get("Investments");
        if (body.contains("business") || body.contains("deal"))
            return categoryNameIdMap.get("Business");
        if (body.contains("grocery") || body.contains("groceries") || body.contains("instamart"))
            return categoryNameIdMap.get("Groceries");
        return categoryNameIdMap.get("Other");
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
