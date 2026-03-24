package com.moneyfi.user.service.jwtservice.impl;

import com.moneyfi.user.model.auth.UserAuthModel;
import com.moneyfi.user.service.jwtservice.JwtService;
import com.moneyfi.user.service.jwtservice.dto.JwtToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.core.internal.Function;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.moneyfi.constants.constants.CommonConstants.userRoleAssociation;

@Slf4j
@Service
public class JwtServiceImplementation implements JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Override
    public JwtToken generateToken(UserAuthModel user, long minutes) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        Key key = Keys.hmacShaKeyFor(keyBytes);

        Map<String, Object> claims = new HashMap<>();

        String role = userRoleAssociation.get(user.getRoleId());
        claims.put("role", role);

        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .claim("userId", user.getId())
                .claim("roles", List.of(role))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + minutes * 60 * 1000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return new JwtToken(token);
    }

    @Override
    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secret)
                .build().parseClaimsJws(token).getBody();
    }

    @Override
    public boolean validateToken(String token, UserDetails userDetails) {
        final String userName = extractUserName(token);
        return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    @Override
    public boolean validateTokenOnly(String token) {
        return !isTokenExpired(token);
    }

    @Override
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("roles", List.class).get(0).toString());
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}

