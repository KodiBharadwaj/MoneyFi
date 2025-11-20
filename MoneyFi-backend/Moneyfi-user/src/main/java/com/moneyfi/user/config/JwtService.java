package com.moneyfi.user.config;

import com.moneyfi.user.repository.ProfileRepository;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final ProfileRepository profileRepository;

    public JwtService(ProfileRepository profileRepository){
        this.profileRepository = profileRepository;
    }

    public String extractUsernameFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
    public Long extractUserIdFromToken(String token) {
        try {
            return profileRepository.getUserIdFromUsernameAndToken(extractUsernameFromToken(token), token);
        } catch (DataAccessException ex) {
            throw new IllegalArgumentException("Token is blacklisted");
        }
    }
}
