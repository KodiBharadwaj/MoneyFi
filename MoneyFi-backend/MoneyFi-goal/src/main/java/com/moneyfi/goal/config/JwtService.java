package com.moneyfi.goal.config;

import com.moneyfi.goal.utils.StringConstants;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final RestTemplate restTemplate;

    public JwtService(RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }

    public Long extractUserIdFromToken(String token) {

        String username = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject(); // Assuming username is stored as the subject

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Long> response = restTemplate.exchange(StringConstants.JWT_SERVICE_API_GATEWAY_URL + username,
                HttpMethod.GET,
                entity,
                Long.class
        );
        return response.getBody();
    }
}
