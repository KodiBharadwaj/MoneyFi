package com.moneyfi.apigateway.service.jwtservice;

import com.moneyfi.apigateway.model.auth.UserAuthModel;
import com.moneyfi.apigateway.service.jwtservice.dto.JwtToken;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {

    JwtToken generateToken(UserAuthModel user);

    String extractUserName(String token);

    boolean validateToken(String token, UserDetails userDetails);
}
