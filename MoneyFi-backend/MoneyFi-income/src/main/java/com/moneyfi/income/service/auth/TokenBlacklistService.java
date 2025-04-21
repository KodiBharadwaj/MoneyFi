package com.moneyfi.income.service.auth;

import com.moneyfi.income.repository.auth.TokenBlackListRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TokenBlacklistService {

    private final TokenBlackListRepository tokenBlacklistRepository;

    public TokenBlacklistService(TokenBlackListRepository tokenBlacklistRepository){
        this.tokenBlacklistRepository = tokenBlacklistRepository;
    }

    public void blacklistToken(com.moneyfi.income.model.auth.BlackListedToken blackListedToken) {
        tokenBlacklistRepository.save(blackListedToken);
    }

    public boolean isTokenBlacklisted(String token) {
        List<com.moneyfi.income.model.auth.BlackListedToken> blackListedTokens = tokenBlacklistRepository.findByToken(token);
        if(blackListedTokens.size() == 0){
            return false;
        }
        return true;
    }

    @Scheduled(fixedRate = 3600000) // Runs every 1 hour
    @Transactional
    public void removeExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        System.out.println("Checking for expired tokens at: " + now);
        tokenBlacklistRepository.deleteByExpiryBefore(now);  // Deletes expired tokens
    }
}

