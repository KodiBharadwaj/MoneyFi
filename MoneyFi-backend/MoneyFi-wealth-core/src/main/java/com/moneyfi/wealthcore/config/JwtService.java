package com.moneyfi.wealthcore.config;

import com.moneyfi.wealthcore.exceptions.ResourceNotFoundException;
import com.moneyfi.wealthcore.repository.budget.BudgetRepository;
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
            throw new IllegalArgumentException("Token is blacklisted");
        } catch (Exception e) {
            throw new ResourceNotFoundException("Failed to validate the user");
        }
    }
}
