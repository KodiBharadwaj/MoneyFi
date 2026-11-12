package com.moneyfi.wealthcore.security;

import com.moneyfi.wealthcore.exceptions.ResourceNotFoundException;
import com.moneyfi.wealthcore.repository.common.WealthCoreRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import static com.moneyfi.constants.constants.CommonConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final WealthCoreRepository wealthCoreRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public Long extractUserIdFromToken(String token) {

        try {
            String username = getUsernameFromToken(token).trim();
            String redisKey = REDIS_BLACKLIST_TOKEN_PREFIX_KEY + DOUBLE_COLON + username + COLON + token;

            try {
                if (!redisTemplate.hasKey(redisKey)) {
                    throw new IllegalArgumentException(TOKEN_BLACKLISTED_MESSAGE);
                }
            } catch (DataAccessException ex) {
                // Redis unavailable: the DB lookup below still validates the session
                log.warn("Redis unavailable, falling back to database session validation for user '{}': {}", username, ex.getMessage());
            }

            return wealthCoreRepository.getUserIdFromUsernameAndToken(username, token);

        } catch (DataAccessException ex) {
            throw new IllegalArgumentException(TOKEN_BLACKLISTED_MESSAGE);
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception e) {
            throw new ResourceNotFoundException(USER_VALIDATION_FAILED_MESSAGE);
        }
    }

    public String getUsernameFromToken(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
    }
}
