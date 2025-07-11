package com.moneyfi.budget.config;

import com.moneyfi.budget.repository.BudgetRepository;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final BudgetRepository budgetRepository;

    public JwtService(BudgetRepository budgetRepository){
        this.budgetRepository = budgetRepository;
    }

    public Long extractUserIdFromToken(String token) {

        String username = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject(); // Assuming username is stored as the subject

        try {
            return budgetRepository.getUserIdFromUsernameAndToken(username, token);
        } catch (DataAccessException ex) {
            // Sql error when the token is blacklisted
            throw new IllegalArgumentException("Token is blacklisted");
        }
    }
}
