package com.moneyfi.apigateway.service;

import com.moneyfi.apigateway.model.auth.BlackListedToken;
import com.moneyfi.apigateway.repository.auth.TokenBlackListRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class TokenBlacklistService {

    private final TokenBlackListRepository tokenBlacklistRepository;

    public TokenBlacklistService(TokenBlackListRepository tokenBlacklistRepository){
        this.tokenBlacklistRepository = tokenBlacklistRepository;
    }

    public BlackListedToken blacklistToken(BlackListedToken blackListedToken) {
        tokenBlacklistRepository.save(blackListedToken);
        return blackListedToken;
    }

    public boolean isTokenBlacklisted(String token) {
        List<BlackListedToken> blackListedTokens = tokenBlacklistRepository.findByToken(token);
        if(blackListedTokens.isEmpty()){
            return false;
        }
        else return true;
    }

    @Scheduled(fixedRate = 3600000) // Runs every 1 hour
    @Transactional
    public void removeExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Checking for expired tokens at: " + now);
        tokenBlacklistRepository.deleteByExpiryBefore(now);  // Deletes expired tokens
    }
}

