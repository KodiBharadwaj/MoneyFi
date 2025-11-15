package com.moneyfi.apigateway.service.userservice.impl;

import com.moneyfi.apigateway.exceptions.ResourceNotFoundException;
import com.moneyfi.apigateway.exceptions.ScenarioNotPossibleException;
import com.moneyfi.apigateway.model.auth.BlackListedToken;
import com.moneyfi.apigateway.model.auth.OtpTempModel;
import com.moneyfi.apigateway.model.auth.SessionTokenModel;
import com.moneyfi.apigateway.model.auth.UserAuthModel;
import com.moneyfi.apigateway.model.common.UserAuthHist;
import com.moneyfi.apigateway.repository.user.UserAuthHistRepository;
import com.moneyfi.apigateway.repository.user.auth.OtpTempRepository;
import com.moneyfi.apigateway.repository.user.auth.UserRepository;
import com.moneyfi.apigateway.service.common.UserCommonService;
import com.moneyfi.apigateway.service.userservice.UserService;
import com.moneyfi.apigateway.service.jwtservice.JwtService;
import com.moneyfi.apigateway.service.jwtservice.dto.JwtToken;
import com.moneyfi.apigateway.service.userservice.dto.request.*;
import com.moneyfi.apigateway.service.userservice.dto.response.RemainingTimeCountDto;
import com.moneyfi.apigateway.util.EmailTemplates;
import com.moneyfi.apigateway.util.enums.*;
import com.moneyfi.apigateway.util.validators.UserValidations;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.moneyfi.apigateway.util.constants.StringUtils.*;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Value("${spring.profiles.active:}")
    private String activeProfile;
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;
    @Value("${spring.security.oauth2.client.registration.github.client-id}")
    private String githubClientId;
    @Value("${spring.security.oauth2.client.registration.github.client-secret}")
    private String githubClientSecret;
    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Autowired
    private RestTemplate externalRestTemplateForOAuth;

    private static final String GOOGLE_TOKEN_END_POINT_URL = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_USER_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";
    private static final String GOOGLE_AUTH_CONSTANT_PASSWORD = null;
    private static final String GITHUB_AUTH_CONSTANT_PASSWORD = null;
    private static final String GITHUB_TOKEN_END_POINT_URL = "https://github.com/login/oauth/access_token";
    private static final String GITHUB_USER_URL = "https://api.github.com/user";
    private static final String GITHUB_USER_EMAILS = "https://api.github.com/user/emails";


    private final UserRepository userRepository;
    private final OtpTempRepository otpTempRepository;
    private final JwtService jwtService;
    private final UserCommonService userCommonService;
    private final EmailTemplates emailTemplates;
    private final UserAuthHistRepository userAuthHistRepository;
    private final AuthenticationManager authenticationManager;

    public UserServiceImpl(UserRepository userRepository,
                           OtpTempRepository otpTempRepository,
                           JwtService jwtService,
                           UserCommonService userCommonService,
                           AuthenticationManager authenticationManager,
                           EmailTemplates emailTemplates,
                           UserAuthHistRepository userAuthHistRepository){
        this.userRepository = userRepository;
        this.otpTempRepository = otpTempRepository;
        this.jwtService = jwtService;
        this.userCommonService = userCommonService;
        this.authenticationManager = authenticationManager;
        this.emailTemplates = emailTemplates;
        this.userAuthHistRepository = userAuthHistRepository;
    }

    @Override
    @Transactional
    public UserAuthModel registerUser(UserProfile userProfile, String loginMode, String address) {
        UserValidations.checkForUserAlreadyExistenceValidation(userRepository.getUserDetailsByUsername(userProfile.getUsername().trim()).orElse(null));
        UserAuthModel userAuthModel = new UserAuthModel();
        saveUserAuthDetails(userAuthModel, userProfile.getUsername());

        int roleId = 0;
        if (loginMode.equalsIgnoreCase(LoginMode.EMAIL_PASSWORD.name())) {
            for (Map.Entry<Integer, String> it : userRoleAssociation.entrySet()) {
                if (it.getValue().equalsIgnoreCase(userProfile.getRole())) {
                    roleId = it.getKey();
                }
            }
            userAuthModel.setPassword(encoder.encode(userProfile.getPassword()));
            userAuthModel.setLoginCodeValue(LoginMode.EMAIL_PASSWORD.getLoginProcessCode());
        } else if (loginMode.equalsIgnoreCase(LoginMode.GOOGLE_OAUTH.name())) {
            for (Map.Entry<Integer, String> it : userRoleAssociation.entrySet()) {
                if (it.getValue().equalsIgnoreCase(UserRoles.USER.name())) {
                    roleId = it.getKey();
                }
            }
            userAuthModel.setPassword(null);
            userAuthModel.setLoginCodeValue(LoginMode.GOOGLE_OAUTH.getLoginProcessCode());
        } else if (loginMode.equalsIgnoreCase(LoginMode.GITHUB_OAUTH.name())) {
            for (Map.Entry<Integer, String> it : userRoleAssociation.entrySet()) {
                if (it.getValue().equalsIgnoreCase(UserRoles.USER.name())) {
                    roleId = it.getKey();
                }
            }
            userAuthModel.setPassword(null);
            userAuthModel.setLoginCodeValue(LoginMode.GITHUB_OAUTH.getLoginProcessCode());
        }
        userAuthModel.setRoleId(roleId);
        UserAuthModel user = userRepository.save(userAuthModel);

        if (!userRoleAssociation.get(user.getRoleId()).equalsIgnoreCase(UserRoles.ADMIN.name())) {
            saveUserProfileDetails(user.getId(), userProfile, address);
            /** send successful email in a separate thread by aws ses **/
            new Thread(() -> {
                try {
//                    awsServices.sendEmailToUserUsingAwsSes(emailTemplates.sendEmailForSuccessfulUserCreation(userProfile.getName(), userProfile.getUsername()));
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException("Exception occurred while sending email: " + e);
                }
            }
            ).start();
        }
        return user;
    }

    private void saveUserAuthDetails(UserAuthModel userAuthModel, String username){
        userAuthModel.setUsername(username.trim());
        userAuthModel.setOtpCount(0);
        userAuthModel.setDeleted(false);
        userAuthModel.setBlocked(false);
    }

    private void saveUserProfileDetails(Long userId, UserProfile userProfile, String address){
        userRepository.insertProfileDetailsDuringSignup(userId, userProfile.getName(), LocalDateTime.now(), (address != null && !address.isEmpty()) ? address : null);
    }

    @Override
    @Transactional
    public ResponseEntity<Map<String, String>> login(UserLoginDetailsRequestDto requestDto) {
        UserAuthModel userAuthModel = new UserAuthModel();
        userAuthModel.setUsername(requestDto.getUsername().trim());
        userAuthModel.setPassword(requestDto.getPassword());
        int roleId = 0;
        for (Map.Entry<Integer, String> it : userRoleAssociation.entrySet()) {
            if (it.getValue().equalsIgnoreCase(requestDto.getRole())) {
                roleId = it.getKey();
            }
        }
        userAuthModel.setRoleId(roleId);
        Map<String, String> userRoleToken = new HashMap<>();
        makeOldSessionInActiveOfUserForNewLogin(userAuthModel.getUsername());
        try {
            if (userAuthModel.getUsername() == null || userAuthModel.getUsername().isEmpty() ||
                    userAuthModel.getPassword() == null || userAuthModel.getPassword().isEmpty()) {
                userRoleToken.put(ERROR, USERNAME_PASSWORD_REQUIRED);
                return ResponseEntity.badRequest().body(userRoleToken);
            }
            UserAuthModel existingUser = userRepository.getUserDetailsByUsername(userAuthModel.getUsername()).orElse(null);
            if (existingUser == null) {
                userRoleToken.put(ERROR, USER_NOT_FOUND_SIGNUP);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(userRoleToken);
            } else if (existingUser.isBlocked()) {
                userRoleToken.put(ERROR, ACCOUNT_BLOCKED);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(userRoleToken);
            } else if (existingUser.isDeleted()) {
                userRoleToken.put(ERROR, ACCOUNT_DELETED);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(userRoleToken);
            }
            try {
                Authentication authentication = authenticationManager
                        .authenticate(new UsernamePasswordAuthenticationToken(userAuthModel.getUsername(), userAuthModel.getPassword()));
                if (authentication.isAuthenticated()) {
                    JwtToken token = jwtService.generateToken(userAuthModel);
                    functionToPreventMultipleLogins(userAuthModel, token);
                    userRoleToken.put(userRoleAssociation.get(existingUser.getRoleId()), token.getJwtToken());
                    return ResponseEntity.ok(userRoleToken);
                }
            } catch (BadCredentialsException ex) {
                ex.printStackTrace();
                userRoleToken.put(ERROR, INCORRECT_PASSWORD);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(userRoleToken);
            }
            userRoleToken.put(ERROR, INVALID_CREDENTIALS);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(userRoleToken);
        } catch (Exception e) {
            e.printStackTrace();
            userRoleToken.put(ERROR, LOGIN_ERROR);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(userRoleToken);
        }
    }

    private void makeOldSessionInActiveOfUserForNewLogin(String username){
        SessionTokenModel sessionTokenUser = userCommonService.getUserByUsername(username);
        if(sessionTokenUser != null && sessionTokenUser.getIsActive()){
            String oldToken = sessionTokenUser.getToken();
            BlackListedToken blackListedToken = new BlackListedToken();
            blackListedToken.setToken(oldToken);
            Date expiryDate = new Date(System.currentTimeMillis() + 3600000);
            blackListedToken.setExpiry(expiryDate);
            userCommonService.blacklistToken(blackListedToken);
        }
    }

    private void functionToPreventMultipleLogins(UserAuthModel userAuthModel, JwtToken token){
        if(userCommonService.getUserByUsername(userAuthModel.getUsername()) != null){
            SessionTokenModel sessionTokens = userCommonService.getUserByUsername(userAuthModel.getUsername());
            sessionTokens.setUsername(userAuthModel.getUsername());
            sessionTokens.setCreatedTime(LocalDateTime.now());
            sessionTokens.setToken(token.getJwtToken());
            sessionTokens.setIsActive(true);
            userCommonService.save(sessionTokens);
        } else {
            SessionTokenModel sessionTokens = new SessionTokenModel();
            sessionTokens.setUsername(userAuthModel.getUsername());
            sessionTokens.setCreatedTime(LocalDateTime.now());
            sessionTokens.setToken(token.getJwtToken());
            sessionTokens.setIsActive(true);
            userCommonService.save(sessionTokens);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<Map<String, String>> loginViaGoogleOAuth(Map<String, String> googleAuthToken) {
        Map<String, String> userRoleToken = new HashMap<>();
        if (googleAuthToken == null || googleAuthToken.isEmpty()) {
            userRoleToken.put(ERROR, "Google Authorization code cannot be null");
            return ResponseEntity.badRequest().body(userRoleToken);
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(mapFunctionToStoreGoogleSecureDetails(googleAuthToken.get("code")), headers);
            // Step 1: Exchange code for tokens
            ResponseEntity<Map> tokenResponse =
                    externalRestTemplateForOAuth.postForEntity(GOOGLE_TOKEN_END_POINT_URL, request, Map.class);
            // Step 2: Verify ID token with Google
            ResponseEntity<Map> userInfoResponse = externalRestTemplateForOAuth.getForEntity(GOOGLE_USER_INFO_URL + (String) tokenResponse.getBody().get("id_token"), Map.class);
            if (userInfoResponse.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> userInfo = userInfoResponse.getBody();
                String email = (String) userInfo.get("email");
                String name = (String) userInfo.get("name");
                String picture = (String) userInfo.get("picture");

                UserAuthModel newOrExistingUser = userRepository.getUserDetailsByUsername(email).orElse(null);
                if (newOrExistingUser == null) {
                    newOrExistingUser = registerUser(new UserProfile((name != null && !name.trim().isEmpty()) ? name : "Google User", email, GOOGLE_AUTH_CONSTANT_PASSWORD, UserRoles.USER.name()), LoginMode.GOOGLE_OAUTH.name(), null);
//                    if(picture != null && !picture.trim().isEmpty()) uploadUserProfilePictureToS3(email, convertImageUrlToMultipartFile(picture));
                }
                if (newOrExistingUser.isBlocked()) {
                    userRoleToken.put(ERROR, ACCOUNT_BLOCKED);
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(userRoleToken);
                } else if (newOrExistingUser.isDeleted()) {
                    userRoleToken.put(ERROR, ACCOUNT_DELETED);
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(userRoleToken);
                }
                makeOldSessionInActiveOfUserForNewLogin(email);
                JwtToken jwtToken = jwtService.generateToken(newOrExistingUser);
                functionToPreventMultipleLogins(newOrExistingUser, jwtToken);
                userRoleToken.put(userRoleAssociation.get(newOrExistingUser.getRoleId()), jwtToken.getJwtToken());
                return ResponseEntity.ok(userRoleToken);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Exception while login via Google OAuth: " + e);
        }
        userRoleToken.put(ERROR, LOGIN_ERROR);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(userRoleToken);
    }

    @Override
    @Transactional
    public String loginViaGithubOAuth(String code) {
        if (code == null || code.isEmpty()) {
            throw new RuntimeException("Authorization code is invalid");
        }
        try {
            String tokenUrl = GITHUB_TOKEN_END_POINT_URL;
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("client_id", githubClientId);
            params.add("client_secret", githubClientSecret);
            params.add("code", code);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            ResponseEntity<Map> tokenResponse = externalRestTemplateForOAuth.postForEntity(tokenUrl, request, Map.class);
            String accessToken = (String) tokenResponse.getBody().get("access_token");
            // 2. Get user info
            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.setBearerAuth(accessToken);
            HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);

            ResponseEntity<Map> userResponse = externalRestTemplateForOAuth.exchange(
                    GITHUB_USER_URL,
                    HttpMethod.GET,
                    userRequest,
                    Map.class
            );
            Map<String, Object> userInfo = userResponse.getBody();
            System.out.println("Checking github output: " + userInfo);
            String email = (String) userInfo.get("email");
            String picture = (String) userInfo.get("avatar_url");
            String name = (String) userInfo.get("name");
            String address = (String) userInfo.get("location");
            if (email == null) {
                ResponseEntity<List> emailResponse = externalRestTemplateForOAuth.exchange(
                        GITHUB_USER_EMAILS,
                        HttpMethod.GET,
                        userRequest,
                        List.class
                );
                List<Map<String, Object>> emails = emailResponse.getBody();
                email = (String) emails.stream()
                        .filter(e -> Boolean.TRUE.equals(e.get("primary")))
                        .findFirst()
                        .map(e -> e.get("email"))
                        .orElse(null);
            }
            // 3. Check or create user in DB
            UserAuthModel user = userRepository.getUserDetailsByUsername(email).orElse(null);
            if (user == null) {
                user = registerUser(new UserProfile((name != null && !name.trim().isEmpty()) ? name : "Github User", email, GITHUB_AUTH_CONSTANT_PASSWORD, UserRoles.USER.name()),
                        LoginMode.GITHUB_OAUTH.name(), (address != null && !address.trim().isEmpty()) ? address : null);
//                if(picture != null && !picture.trim().isEmpty()) uploadUserProfilePictureToS3(email, convertImageUrlToMultipartFile(picture));
            }
            if (user.isBlocked()) {
                throw new ScenarioNotPossibleException("User is blocked, Kindly contact admin");
            } else if (user.isDeleted()) {
                throw new ScenarioNotPossibleException("User is deleted. Kindly contact admin");
            }
            makeOldSessionInActiveOfUserForNewLogin(email);
            // 4. Generate JWT
            JwtToken jwtToken = jwtService.generateToken(user);
            functionToPreventMultipleLogins(user, jwtToken);
            String token = jwtToken.getJwtToken().replace("'", "\\'"); // escape quotes

            String angularOrigin = allowedOrigins.trim(); // wherever your frontend runs
            return "<script>" +
                    "window.opener.postMessage({USER: '" + token + "'}, '" + angularOrigin + "');" +
                    "window.close();" +
                    "</script>";
        } catch (Exception e) {
            e.printStackTrace();
            return "<script>alert('GitHub login failed');window.close();</script>";
        }
    }

    private MultiValueMap<String, String> mapFunctionToStoreGoogleSecureDetails(String code){
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code); // Authentication code from Google comes here via frontend
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("redirect_uri", allowedOrigins.trim()); // your Angular app URL
        params.add("grant_type", "authorization_code");
        return params;
    }

    @Override
    public Long getUserIdByUsername(String email) {
        UserAuthModel userAuthModel = userRepository.getUserDetailsByUsername(email).orElse(null);
        if (userAuthModel == null) {
            return null;
        }
        return userAuthModel.getId();
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordDto changePasswordDto) {
        UserAuthModel user = userRepository.findById(changePasswordDto.getUserId()).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        UserValidations.changePasswordValidations(changePasswordDto, user);
        user.setPassword(encoder.encode(changePasswordDto.getNewPassword()));
        user.setOtpCount(user.getOtpCount() + 1);
        user.setVerificationCodeExpiration(LocalDateTime.now());
        userRepository.save(user);
        userAuthHistRepository.save(new UserAuthHist(changePasswordDto.getUserId(), LocalDateTime.now(), reasonCodeIdAssociation.get(ReasonEnum.PASSWORD_CHANGE), changePasswordDto.getDescription(), changePasswordDto.getUserId()));
        new Thread(() ->
                emailTemplates.sendPasswordChangeAlertMail(userRepository.getUserNameByUsername(user.getUsername()), user.getUsername())
        ).start();
    }

    @Override
    public RemainingTimeCountDto checkOtpActiveMethod(String email) {
        UserAuthModel user = userRepository.getUserDetailsByUsername(email).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        UserValidations.otpSendToUserDuringSignupValidation(user);
        if (user.getVerificationCodeExpiration() == null || user.getVerificationCodeExpiration().isBefore(LocalDateTime.now())) {
            return new RemainingTimeCountDto(0, true);
        }
        return new RemainingTimeCountDto((int) ChronoUnit.MINUTES.between(LocalDateTime.now(), user.getVerificationCodeExpiration()), false);
    }

    @Override
    public String sendOtpForSignup(String email, String name) {
        UserValidations.checkForUserAlreadyExistenceValidation(userRepository.getUserDetailsByUsername(email).orElse(null));
        String verificationCode = generateVerificationCode();
        emailTemplates.sendOtpEmailToUserForSignup(email, name, verificationCode);
        Optional<OtpTempModel> tempModel = otpTempRepository.findByEmail(email)
                .stream()
                .filter(tempOtp -> tempOtp.getOtpType().equalsIgnoreCase(OtpType.USER_CREATION.name()))
                .findFirst();
        if (tempModel.isPresent()) {
            tempModel.get().setOtp(verificationCode);
            tempModel.get().setExpirationTime(LocalDateTime.now().plusMinutes(5));
            otpTempRepository.save(tempModel.get());
        } else {
            otpTempRepository.save(new OtpTempModel(email, verificationCode, LocalDateTime.now().plusMinutes(5), OtpType.USER_CREATION.name()));
        }
        return EMAIL_SENT_SUCCESS_MESSAGE;
    }

    @Override
    public Boolean checkEnteredOtpDuringSignup(String email, String inputOtp) {
        Optional<OtpTempModel> tempModel = otpTempRepository.findByEmail(email)
                .stream()
                .filter(tempOtp -> tempOtp.getOtpType().equalsIgnoreCase(OtpType.USER_CREATION.name())
                        && tempOtp.getOtp().equals(inputOtp) && tempOtp.getExpirationTime().isAfter(LocalDateTime.now()))
                .findFirst();
        if (tempModel.isPresent()) {
            int rowsAffected = otpTempRepository.deleteByEmailAndRequestType(email, OtpType.USER_CREATION.name());
            log.info("Number of OTP records deleted: {}", rowsAffected);
            return true;
        } else throw new ScenarioNotPossibleException(INVALID_OTP_MESSAGE);
    }

    @Override
    @Transactional
    public Map<String, String> logout(String token) {
        Map<String, String> response = new HashMap<>();
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        userRepository.getUserDetailsByUsername(jwtService.extractUserName(token)).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        if (makeUserTokenBlacklisted(token) != null && makeUserSessionInActive(token) != null) {
            response.put(MESSAGE, LOGOUT_SUCCESS_MESSAGE);
        } else {
            response.put(MESSAGE, LOGOUT_FAILURE_MESSAGE);
        }
        return response;
    }

    private BlackListedToken makeUserTokenBlacklisted(String token){
        Date expiryDate = new Date(System.currentTimeMillis());
        BlackListedToken blackListedToken = new BlackListedToken();
        blackListedToken.setToken(token);
        blackListedToken.setExpiry(expiryDate);
        return userCommonService.blacklistToken(blackListedToken);
    }

    private SessionTokenModel makeUserSessionInActive(String token){
        SessionTokenModel sessionTokens = userCommonService.getSessionDetailsByToken(token);
        sessionTokens.setIsActive(false);
        return userCommonService.save(sessionTokens);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public ResponseEntity<String> sendOtpToBlockAccount(String username, String type) {
        UserAuthModel userData = userRepository.getUserDetailsByUsername(username).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        UserValidations.userAlreadyDeactivatedCheckValidation(userData);

        String verificationCode;
        String otpType;
        if (type.equalsIgnoreCase("BLOCK")) {
            otpType = OtpType.ACCOUNT_BLOCK.name();
        } else if (type.equalsIgnoreCase("DELETE")) {
            otpType = OtpType.ACCOUNT_DELETE.name();
        } else {
            otpType = null;
        }
        verificationCode = otpTempRepository.findByEmail(username)
                .stream()
                .filter(tempOtp -> tempOtp.getOtpType().equalsIgnoreCase(otpType) && tempOtp.getExpirationTime().isAfter(LocalDateTime.now()))
                .map(OtpTempModel::getOtp)
                .findFirst()
                .orElseGet(() -> {
                    String newOtp = generateVerificationCode();
                    otpTempRepository.save(new OtpTempModel(username, newOtp, LocalDateTime.now().plusMinutes(5), otpType));
                    return newOtp;
                });
        emailTemplates.sendOtpToUserForAccountBlock(username, userRepository.getUserNameByUsername(username.trim()), verificationCode, type);
        return ResponseEntity.ok(EMAIL_SENT_SUCCESS_MESSAGE);
    }
}
