package com.moneyfi.transaction.config;

import com.moneyfi.transaction.exceptions.ResourceNotFoundException;
import com.moneyfi.transaction.repository.income.IncomeRepository;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final IncomeRepository incomeRepository;

    public JwtService(IncomeRepository incomeRepository){
        this.incomeRepository = incomeRepository;
    }

    public Long extractUserIdFromToken(String token) {
        String username = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        try {
            return incomeRepository.getUserIdFromUsernameAndToken(username, token);
        } catch (DataAccessException ex) {
            throw new IllegalArgumentException("Token is blacklisted");
        } catch (Exception e) {
            throw new ResourceNotFoundException("Failed to validate the user");
        }
    }
}
