package com.moneyfi.transaction.security;

import com.moneyfi.transaction.exceptions.ResourceNotFoundException;
import com.moneyfi.transaction.repository.transaction.TransactionRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.moneyfi.constants.constants.CommonConstants.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final TransactionRepository transactionRepository;

    public Long extractUserIdFromToken(String token, LocalDateTime time) {
        log.info("Security check filter enabled: {}", time);
        try {
            return transactionRepository.getUserIdFromUsernameAndToken(getUsernameFromToken(token).trim(), token);
        } catch (DataAccessException ex) {
            throw new IllegalArgumentException(TOKEN_BLACKLISTED_MESSAGE);
        } catch (Exception e) {
            throw new ResourceNotFoundException(USER_VALIDATION_FAILED_MESSAGE);
        }
    }

    public String getUsernameFromToken(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractUserIdFromToken(String token) {
        return extractAllClaims(token).get(USER_ID, Long.class);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
    }
}
