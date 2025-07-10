package com.moneyfi.goal.config;

import com.moneyfi.goal.repository.GoalRepository;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final GoalRepository goalRepository;

    public JwtService(GoalRepository goalRepository){
        this.goalRepository = goalRepository;
    }

    public Long extractUserIdFromToken(String token) {

        String username = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject(); // Assuming username is stored as the subject

        try {
            return goalRepository.getUserIdFromUsernameAndToken(username, token);
        } catch (DataAccessException ex) {
            // Sql error when the token is blacklisted
            throw new IllegalArgumentException("Token is blacklisted");
        }
    }
}
