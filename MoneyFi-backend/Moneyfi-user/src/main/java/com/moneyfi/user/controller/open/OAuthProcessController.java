package com.moneyfi.user.controller.open;

import com.moneyfi.user.service.user.UserAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user-service/Oauth")
@RequiredArgsConstructor
public class OAuthProcessController {

    @Value("${spring.security.oauth2.client.registration.facebook.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.facebook.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.facebook.redirect-uri}")
    private String redirectUri;

    private final UserAuthService userAuthService;
    private final RestTemplate externalRestTemplateForOAuth;

    @PostMapping("/google/callback")
    public ResponseEntity<Map<String, String>> handleGoogleCallback(@RequestBody Map<String, String> googleAuthToken) {
        return userAuthService.loginViaGoogleOAuth(googleAuthToken);
    }

    @GetMapping("/github/popup-callback")
    public String handleGithubPopup(@RequestParam("code") String code) {
        return userAuthService.loginViaGithubOAuth(code);
    }

    @GetMapping("/facebook/callback")
    public ResponseEntity<?> facebookCallback(@RequestParam("code") String code) {
        try {
            // 1. Exchange code for access token
            String tokenUrl = "https://graph.facebook.com/v16.0/oauth/access_token" +
                    "?client_id=" + clientId +
                    "&redirect_uri=" + redirectUri +
                    "&client_secret=" + clientSecret +
                    "&code=" + code;

            Map<String, Object> tokenResponse = externalRestTemplateForOAuth.getForObject(tokenUrl, Map.class);
            String accessToken = (String) tokenResponse.get("access_token");

            // 2. Fetch user info
            String userInfoUrl = "https://graph.facebook.com/me?fields=id,name,email&access_token=" + accessToken;
            Map<String, Object> userInfo = externalRestTemplateForOAuth.getForObject(userInfoUrl, Map.class);

            System.out.println("checking userinfo: " + userInfo);

            // 3. Map to your internal user and generate JWT
            String email = (String) userInfo.get("email");
            String name = (String) userInfo.get("name");
            String facebookId = (String) userInfo.get("id");

            // TODO: check user in DB, create if not exists
            // String jwt = jwtService.generateToken(user);

            Map<String, Object> response = new HashMap<>();
            response.put("name", name);
            response.put("email", email);
            response.put("facebookId", facebookId);
            // response.put("jwt", jwt);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Facebook login failed: " + e.getMessage());
        }
    }
}
