package com.moneyfi.apigateway.service.jwtservice;

import com.moneyfi.apigateway.service.jwtservice.dto.JwtToken;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {

    JwtToken generateToken(String username);

    String extractUserName(String token);

    boolean validateToken(String token, UserDetails userDetails);
}
