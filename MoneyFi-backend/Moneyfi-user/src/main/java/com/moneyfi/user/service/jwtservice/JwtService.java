package com.moneyfi.user.service.jwtservice;

import com.moneyfi.user.model.auth.UserAuthModel;
import com.moneyfi.user.service.jwtservice.dto.JwtToken;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {

    JwtToken generateToken(UserAuthModel user, long minutes);

    String extractUserName(String token);

    boolean validateToken(String token, UserDetails userDetails);

    boolean validateTokenOnly(@NotBlank String token);

    String extractRole(@NotBlank String token);
}
