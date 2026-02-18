package com.moneyfi.apigateway.service.gmailsync;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.moneyfi.apigateway.dto.ParsedTransaction;
import com.moneyfi.apigateway.exceptions.CustomAuthenticationFailedException;
import com.moneyfi.apigateway.exceptions.ResourceNotFoundException;
import com.moneyfi.apigateway.exceptions.ScenarioNotPossibleException;
import com.moneyfi.apigateway.model.gmailsync.GmailAuth;
import com.moneyfi.apigateway.model.gmailsync.GmailProcessedMessageEntity;
import com.moneyfi.apigateway.model.gmailsync.GmailSyncHistory;
import com.moneyfi.apigateway.repository.common.CommonServiceRepository;
import com.moneyfi.apigateway.repository.gmailsync.GmailSyncHistoryRepository;
import com.moneyfi.apigateway.repository.gmailsync.GmailSyncRepository;
import com.moneyfi.apigateway.repository.gmailsync.GmailProcessedMessageRepository;
import com.moneyfi.apigateway.service.general.GoogleOAuthEndPointDealerService;
import com.moneyfi.apigateway.service.gmailsync.dto.response.GmailSyncHistoryResponse;
import com.moneyfi.apigateway.util.enums.TransactionServiceType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.moneyfi.apigateway.util.constants.StringConstants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GmailSyncService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    private final GmailSyncRepository gmailSyncRepository;
    private final GmailProcessedMessageRepository processedRepository;
    private final CommonServiceRepository commonServiceRepository;
    private final GmailSyncHistoryRepository gmailSyncHistoryRepository;
    private final GoogleOAuthEndPointDealerService googleOAuthEndPointDealerService;

    private static final String FUTURE_SYNC_NOT_ALLOWED = "Future date sync is not allowed";
    private static final String CONSENT_NOT_FOUND = "User consent not found";
    private static final String USER_NOT_ALLOWED_TO_SYNC = "User not allowed to sync";
    private static final String SYNC_LIMIT_CROSSED_TODAY_MESSAGE = "Sync limit crossed for today. Please try tomorrow";
    private static final String UNAUTHORIZED_USER = "Unauthorized Gmail user";
    private static final String STRING_ME = "me";

    @Transactional(rollbackOn = Exception.class)
    public void enableSync(String code, String username, Long userId) {
        if (StringUtils.isBlank(code)) {
            throw new ScenarioNotPossibleException(GOOGLE_AUTHORIZATION_CODE_NULL_MESSAGE);
        }
        Map<String, Object> tokenResponse = googleOAuthEndPointDealerService.exchangeAuthorizationCodeAndGetAccessRefreshTokens(code);
        String accessToken = (String) tokenResponse.get(ACCESS_TOKEN);
        String refreshToken = (String) tokenResponse.get(REFRESH_TOKEN);
        Number expiresIn = (Number) tokenResponse.get(EXPIRES_IN);

        googleOAuthEndPointDealerService.securityValidationCheckToVerifyToken(GMAIL_SYNC, accessToken);

        Map<String, Object> userInfo = googleOAuthEndPointDealerService.getUserInformationFromAccessToken(accessToken);
        String gmailFromCode = ((String) userInfo.get(STRING_EMAIL)).trim();

        if (!username.trim().equalsIgnoreCase(gmailFromCode)) {
            throw new CustomAuthenticationFailedException(UNAUTHORIZED_USER);
        }
        GmailAuth auth = gmailSyncRepository.findByUserId(userId).orElseGet(GmailAuth::new);
        googleOAuthEndPointDealerService.setGmailAuthDetails(auth, userId, accessToken, refreshToken, expiresIn);
        gmailSyncRepository.save(auth);
    }

    public Integer getGmailConsentStatus(Long userId) {
        Optional<GmailAuth> gmailAuth = gmailSyncRepository.findByUserId(userId);
        return (gmailAuth.isPresent() && Boolean.TRUE.equals(gmailAuth.get().getIsActive())) ? gmailAuth.get().getCount() : null;
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

    @Transactional(rollbackOn = Exception.class)
    public Map<Integer, List<ParsedTransaction>> startGmailSync(Long userId, LocalDate date) throws IOException, URISyntaxException {
        if (date.isAfter(LocalDate.now())) {
            throw new ScenarioNotPossibleException(FUTURE_SYNC_NOT_ALLOWED);
        }
        GmailAuth gmailAuth = gmailSyncRepository.findByUserId(userId).filter(GmailAuth::getIsActive).orElseThrow(() -> new ResourceNotFoundException(CONSENT_NOT_FOUND));
        if(gmailAuth != null && gmailAuth.getCount() >= 3) throw new ScenarioNotPossibleException(SYNC_LIMIT_CROSSED_TODAY_MESSAGE);
        else if(gmailAuth == null) throw new ScenarioNotPossibleException(USER_NOT_ALLOWED_TO_SYNC);

        gmailAuth.setCount((gmailAuth.getCount() != null ? gmailAuth.getCount() : 0) + 1);
        gmailSyncRepository.save(gmailAuth);
        /** return new HashMap<>(Map.of(2, List.of(new ParsedTransaction(1, "Upi", new BigDecimal("123"), "CREDIT OR DEBIT", LocalDateTime.now())))); **/
        return Map.of(3 - gmailAuth.getCount(), syncEmails(userId, date).stream().filter(Objects::nonNull).toList());
    }

    private List<ParsedTransaction> syncEmails(Long userId, LocalDate date) throws IOException, URISyntaxException {
        Gmail gmail = googleOAuthEndPointDealerService.gmailClientForUser(gmailSyncRepository, userId);
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
        ListMessagesResponse response = gmail.users().messages().list(STRING_ME)
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
            Message fullMessage = gmail.users().messages().get(STRING_ME, msg.getId()).execute();
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
        boolean debit =
                body.contains("debited") || body.contains("debit") ||
                body.contains("spent") || body.contains("settlement") ||
                body.contains("withdrawn") || body.contains("transaction") ||
                body.contains("purchase") || body.contains("purchased") ||
                body.contains("transferred") || body.contains("transfer") ||
                body.contains("paid") || body.contains("used") || body.contains("has been used");

        boolean credit =
                body.contains("credited") || body.contains("credit") ||
                        body.contains("received") || body.contains("settlement") || body.contains("refund");
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

    private String getTextFromPayload(MessagePart part) {
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
