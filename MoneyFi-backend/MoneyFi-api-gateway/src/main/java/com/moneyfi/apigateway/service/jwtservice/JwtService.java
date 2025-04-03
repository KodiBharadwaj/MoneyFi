package com.moneyfi.apigateway.service.jwtservice;

import com.moneyfi.apigateway.dto.JwtToken;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {

    public JwtToken generateToken(String username);

    public String extractUserName(String token);

    public boolean validateToken(String token, UserDetails userDetails);
}
