package com.moneyfi.transaction.batch.service.general;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BatchAuthTokenStore {

    private final Map<String, String> tokenStore =
            new ConcurrentHashMap<>();

    public void put(String requestId, String token) {
        tokenStore.put(requestId, token);
    }

    public String get(String requestId) {
        return tokenStore.get(requestId);
    }

    public void remove(String requestId) {
        tokenStore.remove(requestId);
    }
}
