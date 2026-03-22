package com.moneyfi.user.service.user.impl;

import com.moneyfi.constants.enums.*;
import com.moneyfi.user.exceptions.ResourceNotFoundException;
import com.moneyfi.user.exceptions.ScenarioNotPossibleException;
import com.moneyfi.user.model.auth.*;
import com.moneyfi.user.model.general.ProfileModel;
import com.moneyfi.user.model.gmailsync.GmailAuth;
import com.moneyfi.user.repository.general.ProfileRepository;
import com.moneyfi.user.repository.gmailsync.GmailSyncRepository;
import com.moneyfi.user.repository.auth.UserAuthHistRepository;
import com.moneyfi.user.repository.auth.OtpTempRepository;
import com.moneyfi.user.repository.auth.UserRepository;
import com.moneyfi.user.service.user.UserAuthService;
import com.moneyfi.user.service.general.caching.UserCacheService;
import com.moneyfi.user.service.user.UserCommonService;
import com.moneyfi.user.service.user.dto.internal.NotificationQueueDto;
import com.moneyfi.user.service.user.dto.request.ChangePasswordDto;
import com.moneyfi.user.service.user.dto.request.UserLoginDetailsRequestDto;
import com.moneyfi.user.service.user.dto.request.UserProfile;
import com.moneyfi.user.service.user.dto.response.RemainingTimeCountDto;
import com.moneyfi.user.service.general.external.endpoint.GoogleOAuthEndPointDealerService;
import com.moneyfi.user.service.jwtservice.JwtService;
import com.moneyfi.user.service.jwtservice.dto.JwtToken;
import com.moneyfi.user.service.user.ProfileService;
import com.moneyfi.user.util.constants.StringConstants;
import com.moneyfi.user.util.enums.ResendOtpType;
import com.moneyfi.user.validator.UserValidations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.moneyfi.constants.constants.CommonConstants.*;
import static com.moneyfi.user.util.constants.StringConstants.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserAuthServiceImpl implements UserAuthService {

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

    private final UserRepository userRepository;
    private final UserAuthHistRepository userAuthHistRepository;
    private final GoogleOAuthEndPointDealerService googleOAuthEndPointDealerService;
    private final UserCommonService userCommonService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final GmailSyncRepository gmailSyncRepository;
    private final OtpTempRepository otpTempRepository;
    private final UserCacheService userCacheService;
    private final ProfileService profileService;
    private final ProfileRepository profileRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    private static final String GOOGLE_AUTH_CONSTANT_PASSWORD = null;
    private static final String GITHUB_AUTH_CONSTANT_PASSWORD = null;
    private static final String GITHUB_TOKEN_END_POINT_URL = "https://github.com/login/oauth/access_token";
    private static final String GITHUB_USER_URL = "https://api.github.com/user";
    private static final String GITHUB_USER_EMAILS = "https://api.github.com/user/emails";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserAuthModel registerUser(UserProfile userProfile, String loginMode, String address) {
        UserValidations.checkForUserAlreadyExistenceValidation(userRepository.getUserDetailsByUsername(userProfile.getUsername().trim()).orElse(null));
        UserAuthModel userAuthModel = new UserAuthModel();
        setUserAuthDetails(userAuthModel, userProfile.getUsername());

        if (loginMode.equalsIgnoreCase(LoginMode.EMAIL_PASSWORD.name())) {
            userAuthModel.setPassword(StringConstants.encoder.encode(userProfile.getPassword()));
            userAuthModel.setLoginCodeValue(LoginMode.EMAIL_PASSWORD.getLoginProcessCode());
        } else if (loginMode.equalsIgnoreCase(LoginMode.GOOGLE_OAUTH.name())) {
            userAuthModel.setPassword(null);
            userAuthModel.setLoginCodeValue(LoginMode.GOOGLE_OAUTH.getLoginProcessCode());
        } else if (loginMode.equalsIgnoreCase(LoginMode.GITHUB_OAUTH.name())) {
            userAuthModel.setPassword(null);
            userAuthModel.setLoginCodeValue(LoginMode.GITHUB_OAUTH.getLoginProcessCode());
        }
        userAuthModel.setRoleId(StringConstants.functionToGetRoleIdBasedOnRoleName(userProfile.getRole()));
        UserAuthModel user = userRepository.save(userAuthModel);

        if (!userRoleAssociation.get(user.getRoleId()).equalsIgnoreCase(UserRoles.ADMIN.name())) {
            saveUserProfileDetails(user.getId(), userProfile, address);
            applicationEventPublisher.publishEvent(new NotificationQueueDto(NotificationQueueEnum.USER_ACCOUNT_CREATION_MAIL.name(), user.getUsername() + "<|>" + userProfile.getName()));
        }
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Map<String, String>> login(UserLoginDetailsRequestDto requestDto) {
        UserAuthModel userAuthModel = new UserAuthModel();
        userAuthModel.setUsername(requestDto.getUsername().trim());
        userAuthModel.setPassword(requestDto.getPassword());
        userAuthModel.setRoleId(StringConstants.functionToGetRoleIdBasedOnRoleName(requestDto.getRole()));

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
                    JwtToken token = jwtService.generateToken(existingUser, SESSION_LOGIN_MINUTES);
                    functionToPreventMultipleLogins(userAuthModel, token);
                    makeGmailAuthInactiveForUser(getUserIdByUsername(requestDto.getUsername().trim()));
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Map<String, String>> loginViaGoogleOAuth(Map<String, String> googleAuthToken) {
        Map<String, String> userRoleToken = new HashMap<>();
        if (googleAuthToken == null || googleAuthToken.isEmpty()) {
            userRoleToken.put(ERROR, GOOGLE_AUTHORIZATION_CODE_NULL_MESSAGE);
            return ResponseEntity.badRequest().body(userRoleToken);
        }
        try {
            Map<String, Object> tokenResponse = googleOAuthEndPointDealerService.exchangeAuthorizationCodeAndGetAccessRefreshTokens(googleAuthToken.get(CODE));
            String accessToken = (String) tokenResponse.get(ACCESS_TOKEN);
            String refreshToken = (String) tokenResponse.get(REFRESH_TOKEN);
            Number expiresIn = (Number) tokenResponse.get(EXPIRES_IN);
            googleOAuthEndPointDealerService.securityValidationCheckToVerifyToken("GMAIL_SYNC", accessToken);
            Map<String, Object> userInfo = googleOAuthEndPointDealerService.getUserInformationFromAccessToken(accessToken);

            String email = ((String) userInfo.get(STRING_EMAIL)).trim();
            String name = (String) userInfo.get("name");
            String picture = (String) userInfo.get("picture");

            UserAuthModel newOrExistingUser = userRepository.getUserDetailsByUsername(email).orElse(null);
            if (newOrExistingUser == null) {
                newOrExistingUser = registerUser(new UserProfile((name != null && !name.trim().isEmpty()) ? name : "Google User", email, GOOGLE_AUTH_CONSTANT_PASSWORD, UserRoles.USER.name()), LoginMode.GOOGLE_OAUTH.name(), null);
                if (picture != null && !picture.trim().isEmpty())
                    userCommonService.uploadUserProfilePictureToS3(newOrExistingUser.getUsername(), newOrExistingUser.getId(), convertImageUrlToMultipartFile(picture));
            }

            GmailAuth newAuth = new GmailAuth();
            Optional<GmailAuth> gmailAuth = gmailSyncRepository.findByUserId(newOrExistingUser.getId());
            if (gmailAuth.isPresent()) newAuth = gmailAuth.get();
            googleOAuthEndPointDealerService.setGmailAuthDetails(newAuth, newOrExistingUser.getId(), accessToken, refreshToken, expiresIn);
            gmailSyncRepository.save(newAuth);

            if (newOrExistingUser.isBlocked()) {
                userRoleToken.put(ERROR, ACCOUNT_BLOCKED);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(userRoleToken);
            } else if (newOrExistingUser.isDeleted()) {
                userRoleToken.put(ERROR, ACCOUNT_DELETED);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(userRoleToken);
            }
            makeOldSessionInActiveOfUserForNewLogin(email);
            JwtToken jwtToken = jwtService.generateToken(newOrExistingUser, SESSION_LOGIN_MINUTES);
            functionToPreventMultipleLogins(newOrExistingUser, jwtToken);
            userRoleToken.put(userRoleAssociation.get(newOrExistingUser.getRoleId()), jwtToken.getJwtToken());
            return ResponseEntity.ok(userRoleToken);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Exception while login via Google OAuth: " + e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String loginViaGithubOAuth(String code) {
        if (code == null || code.isEmpty()) {
            throw new RuntimeException("Authorization code is invalid");
        }
        try {
            String tokenUrl = GITHUB_TOKEN_END_POINT_URL;
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add(CLIENT_ID, githubClientId);
            params.add(CLIENT_SECRET, githubClientSecret);
            params.add(CODE, code);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            ResponseEntity<Map> tokenResponse = externalRestTemplateForOAuth.postForEntity(tokenUrl, request, Map.class);
            String accessToken = (String) tokenResponse.getBody().get(ACCESS_TOKEN);
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
            String email = (String) userInfo.get(STRING_EMAIL);
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
                        .map(e -> e.get(STRING_EMAIL))
                        .orElse(null);
            }
            // 3. Check or create user in DB
            UserAuthModel user = userRepository.getUserDetailsByUsername(email).orElse(null);
            if (user == null) {
                user = registerUser(new UserProfile((name != null && !name.trim().isEmpty()) ? name : "Github User", email, GITHUB_AUTH_CONSTANT_PASSWORD, UserRoles.USER.name()),
                        LoginMode.GITHUB_OAUTH.name(), (address != null && !address.trim().isEmpty()) ? address : null);
                if (picture != null && !picture.trim().isEmpty())
                    userCommonService.uploadUserProfilePictureToS3(user.getUsername(), user.getId(), convertImageUrlToMultipartFile(picture));
            }
            if (user.isBlocked()) {
                throw new ScenarioNotPossibleException("User is blocked, Kindly contact admin");
            } else if (user.isDeleted()) {
                throw new ScenarioNotPossibleException("User is deleted. Kindly contact admin");
            }
            makeOldSessionInActiveOfUserForNewLogin(email);
            // 4. Generate JWT
            JwtToken jwtToken = jwtService.generateToken(user, SESSION_LOGIN_MINUTES);
            functionToPreventMultipleLogins(user, jwtToken);
            String token = jwtToken.getJwtToken().replace("'", "\\'"); // escape quotes

            String[] angularOrigin = Arrays.stream(allowedOrigins.split(",")).map(String::trim).toArray(String[]::new); // wherever your frontend runs
            return "<script>" +
                    "window.opener.postMessage({USER: '" + token + "'}, '" + angularOrigin[0] + "');" +
                    "window.close();" +
                    "</script>";
        } catch (Exception e) {
            e.printStackTrace();
            return "<script>alert('GitHub login failed');window.close();</script>";
        }
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
    @Transactional(rollbackFor = Exception.class)
    public String forgotPassword(String email) {
        UserAuthModel user = userRepository.getUserDetailsByUsername(email).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        UserValidations.userAlreadyDeactivatedCheckValidation(user);
        String verificationCode = null;
        if (user.getVerificationCode() != null && LocalDateTime.now().isBefore(user.getVerificationCodeExpiration())) {
            verificationCode = user.getVerificationCode();
        } else {
            verificationCode = generateVerificationCode();
            user.setVerificationCode(verificationCode);
            user.setVerificationCodeExpiration(LocalDateTime.now().plusMinutes(5));
            user.setOtpCount(user.getOtpCount() + 1);
            userRepository.save(user);
        }
        applicationEventPublisher.publishEvent(new NotificationQueueDto(NotificationQueueEnum.OTP_FOR_FORGOT_PASSWORD.name(), profileService.getUserDetailsByUserId(user.getId()) + "<|>" + email + "<|>" + verificationCode));
        return VERIFICATION_CODE_SENT_MESSAGE;
    }

    @Override
    public String verifyCode(String email, String code) {
        UserAuthModel user = userRepository.getUserDetailsByUsername(email).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        boolean response = user.getVerificationCode().equals(code) && LocalDateTime.now().isBefore(user.getVerificationCodeExpiration());
        if (response) return VERIFICATION_SUCCESSFUL_MESSAGE;
        else throw new ScenarioNotPossibleException(VERIFICATION_FAILURE_MESSAGE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updatePasswordOnUserForgotMode(String email, String password){
        UserAuthModel user = userRepository.getUserDetailsByUsername(email).orElseThrow(()-> new ResourceNotFoundException(USER_NOT_FOUND));
        if(encoder.matches(password, user.getPassword())) {
            throw new ScenarioNotPossibleException(SAME_PASSWORD_NOT_ALLOWED_MESSAGE);
        }
        user.setPassword(encoder.encode(password));
        user.setVerificationCode(null);
        user.setVerificationCodeExpiration(null);
        userRepository.save(user);
        userAuthHistRepository.save(new UserAuthHist(user.getId(), LocalDateTime.now(), reasonCodeIdAssociation.get(ReasonEnum.FORGOT_PASSWORD), PASSWORD_UPDATED_MODE_USING_FORGOT, user.getId()));
        return PASSWORD_UPDATED_SUCCESSFULLY;
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
    public void resendOtp(String username, String type) {
        if (StringUtils.isBlank(username)) throw new ScenarioNotPossibleException("Username cannot be empty");
        if (ResendOtpType.BLOCK.name().equalsIgnoreCase(type) || ResendOtpType.DELETE.name().equalsIgnoreCase(type)) {
            resendOtpForUserBlockOrDeleteOperation(username, type);
        } else if (ResendOtpType.FORGOT_PASSWORD.name().equalsIgnoreCase(type)) {
            resendOtpForForgotPasswordOperation(username);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> logout(String token) {
        Map<String, String> response = new HashMap<>();
        String username = jwtService.extractUserName(token);
        UserAuthModel user = userRepository.getUserDetailsByUsername(username).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        makeGmailAuthInactiveForUser(getUserIdByUsername(username));
        if (makeUserTokenBlacklisted(token, username) != null && makeUserSessionInActive(token) != null) {
            response.put(MESSAGE, LOGOUT_SUCCESS_MESSAGE);
        } else {
            response.put(MESSAGE, LOGOUT_FAILURE_MESSAGE);
        }
        new Thread(() -> functionCallToRemoveDataFromCache(user)).start();
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateUserSessionExpirationTime(long minutes, String username, String token) {
        if (minutes == 0) {
            throw new ScenarioNotPossibleException("Minutes cannot be zero");
        }
        UserAuthModel user = userRepository.getUserDetailsByUsername(username).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        makeUserTokenBlacklisted(token, user.getUsername());
        String newToken = jwtService.generateToken(user, minutes).getJwtToken();
        functionToPreventMultipleLogins(user, new JwtToken(newToken));
        return newToken;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(ChangePasswordDto changePasswordDto) {
        UserAuthModel user = userRepository.findById(changePasswordDto.getUserId()).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        UserValidations.changePasswordValidations(changePasswordDto, user);
        user.setPassword(encoder.encode(changePasswordDto.getNewPassword()));
        user.setOtpCount(user.getOtpCount() + 1);
        user.setVerificationCodeExpiration(LocalDateTime.now());
        userRepository.save(user);
        userAuthHistRepository.save(new UserAuthHist(changePasswordDto.getUserId(), LocalDateTime.now(), reasonCodeIdAssociation.get(ReasonEnum.PASSWORD_CHANGE), changePasswordDto.getDescription(), changePasswordDto.getUserId()));
        applicationEventPublisher.publishEvent(new NotificationQueueDto(NotificationQueueEnum.USER_PASSWORD_CHANGE_ALERT_MAIL.name(), profileService.getUserDetailsByUserId(user.getId()) + "<|>" + user.getUsername()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<String> sendOtpToBlockAccount(String username, String type) {
        UserAuthModel userData = userRepository.getUserDetailsByUsername(username).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        UserValidations.userAlreadyDeactivatedCheckValidation(userData);

        String otpType = functionToGetOtpType(type);
        String verificationCode = otpTempRepository.findByEmail(username).stream()
                .filter(tempOtp -> tempOtp.getOtpType().equalsIgnoreCase(otpType) && tempOtp.getExpirationTime().isAfter(LocalDateTime.now()))
                .map(OtpTempModel::getOtp)
                .findFirst()
                .orElseGet(() -> {
                    String newOtp = generateVerificationCode();
                    otpTempRepository.save(new OtpTempModel(username, newOtp, LocalDateTime.now().plusMinutes(5), otpType));
                    return newOtp;
                });
        applicationEventPublisher.publishEvent(new NotificationQueueDto(NotificationQueueEnum.OTP_FOR_USER_BLOCK.name(), username + "<|>" + profileService.getUserDetailsByUserId(userData.getId()) + "<|>" + verificationCode + "<|>" + type));
        return ResponseEntity.ok(EMAIL_SENT_SUCCESS_MESSAGE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String sendOtpForSignup(String email, String name) {
        UserValidations.checkForUserAlreadyExistenceValidation(userRepository.getUserDetailsByUsername(email).orElse(null));
        String verificationCode = generateVerificationCode();
        applicationEventPublisher.publishEvent(new NotificationQueueDto(NotificationQueueEnum.OTP_MAIL_FOR_USER_SIGNUP.name(), email + "<|>" + name + "<|>" + verificationCode));

        Optional<OtpTempModel> tempModel = otpTempRepository.findByEmail(email).stream()
                .filter(tempOtp -> tempOtp.getOtpType().equalsIgnoreCase(OtpType.USER_CREATION.name()))
                .findFirst();
        if (tempModel.isPresent()) {
            OtpTempModel existing = tempModel.get();
            existing.setOtp(verificationCode);
            existing.setExpirationTime(LocalDateTime.now().plusMinutes(5));
            otpTempRepository.save(existing);
        } else {
            otpTempRepository.save(new OtpTempModel(email, verificationCode, LocalDateTime.now().plusMinutes(5), OtpType.USER_CREATION.name()));
        }
        return EMAIL_SENT_SUCCESS_MESSAGE;
    }

    @Override
    public Boolean checkEnteredOtpDuringSignup(String email, String inputOtp) {
        Optional<OtpTempModel> tempModel = otpTempRepository.findByEmail(email).stream()
                .filter(tempOtp -> tempOtp.getOtpType().equalsIgnoreCase(OtpType.USER_CREATION.name())
                        && tempOtp.getOtp().equals(inputOtp) && tempOtp.getExpirationTime().isAfter(LocalDateTime.now()))
                .findFirst();
        if (tempModel.isPresent()) {
            int rowsAffected = otpTempRepository.deleteByEmailAndRequestType(email, OtpType.USER_CREATION.name());
            log.info("Number of OTP records deleted: {}", rowsAffected);
            return true;
        } else throw new ScenarioNotPossibleException(INVALID_OTP_MESSAGE);
    }

    private void setUserAuthDetails(UserAuthModel userAuthModel, String username) {
        userAuthModel.setUsername(username.trim());
        userAuthModel.setOtpCount(0);
        userAuthModel.setDeleted(Boolean.FALSE);
        userAuthModel.setBlocked(Boolean.FALSE);
    }

    private void saveUserProfileDetails(Long userId, UserProfile userProfile, String address) {
        ProfileModel profileModel = new ProfileModel();
        profileModel.setUserId(userId);
        profileModel.setName(userProfile.getName());
        profileModel.setCreatedDate(LocalDateTime.now());
        profileModel.setAddress(address);
        profileRepository.save(profileModel);
    }

    private void makeOldSessionInActiveOfUserForNewLogin(String username) {
        SessionTokenModel sessionTokenUser = userCommonService.getUserByUsername(username);
        if (sessionTokenUser != null && sessionTokenUser.getIsActive()) {
            makeUserTokenBlacklisted(sessionTokenUser.getToken(), username);
        }
    }

    private BlackListedToken makeUserTokenBlacklisted(String token, String username) {
        return userCommonService.blacklistToken(new BlackListedToken(token, new Date(System.currentTimeMillis()), username));
    }

    private void functionToPreventMultipleLogins(UserAuthModel userAuthModel, JwtToken token) {
        if (userCommonService.getUserByUsername(userAuthModel.getUsername()) != null) {
            SessionTokenModel sessionTokens = userCommonService.getUserByUsername(userAuthModel.getUsername());
            sessionTokens.setUsername(userAuthModel.getUsername());
            sessionTokens.setCreatedTime(LocalDateTime.now());
            sessionTokens.setToken(token.getJwtToken());
            sessionTokens.setIsActive(Boolean.TRUE);
            userCommonService.saveSessionTokenModel(sessionTokens);
        } else {
            SessionTokenModel sessionTokens = new SessionTokenModel();
            sessionTokens.setUsername(userAuthModel.getUsername());
            sessionTokens.setCreatedTime(LocalDateTime.now());
            sessionTokens.setToken(token.getJwtToken());
            sessionTokens.setIsActive(Boolean.TRUE);
            userCommonService.saveSessionTokenModel(sessionTokens);
        }
    }

    private void makeGmailAuthInactiveForUser(Long userId) {
        GmailAuth gmailAuth = gmailSyncRepository.findByUserId(userId).orElse(null);
        if (gmailAuth == null) return;
        gmailAuth.setIsActive(Boolean.FALSE);
        gmailSyncRepository.save(gmailAuth);
    }

    private void resendOtpForUserBlockOrDeleteOperation(String username, String type) {
        String otpType = functionToGetOtpType(type);
        String verificationCode = otpTempRepository.findByEmail(username).stream()
                .filter(tempOtp -> tempOtp.getOtpType().equalsIgnoreCase(otpType) && tempOtp.getExpirationTime().isAfter(LocalDateTime.now()))
                .map(OtpTempModel::getOtp)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(OTP_RESEND_FAILURE_MESSAGE));
        applicationEventPublisher.publishEvent(new NotificationQueueDto(NotificationQueueEnum.OTP_FOR_USER_BLOCK.name(), username + "<|>" + profileService.getUserDetailsByUserId(getUserIdByUsername(username)) + "<|>" + verificationCode + "<|>" + type));
    }

    private String functionToGetOtpType(String type) {
        if (type.equalsIgnoreCase(ResendOtpType.BLOCK.name())) {
            return OtpType.ACCOUNT_BLOCK.name();
        } else if (type.equalsIgnoreCase(ResendOtpType.DELETE.name())) {
            return OtpType.ACCOUNT_DELETE.name();
        } else {
            return null;
        }
    }

    private void resendOtpForForgotPasswordOperation(String username) {
        UserAuthModel user = userRepository.getUserDetailsByUsername(username).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        if (StringUtils.isNotBlank(user.getVerificationCode()) && user.getVerificationCodeExpiration() != null && user.getVerificationCodeExpiration().isAfter(LocalDateTime.now())) {
            applicationEventPublisher.publishEvent(new NotificationQueueDto(NotificationQueueEnum.OTP_FOR_FORGOT_PASSWORD.name(), profileService.getUserDetailsByUserId(user.getId()) + "<|>" + username + "<|>" + user.getVerificationCode()));
        } else throw new ScenarioNotPossibleException(OTP_RESEND_FAILURE_MESSAGE);
    }

    private SessionTokenModel makeUserSessionInActive(String token) {
        SessionTokenModel sessionTokens = userCommonService.getSessionDetailsByToken(token);
        sessionTokens.setIsActive(Boolean.FALSE);
        return userCommonService.save(sessionTokens);
    }

    private void functionCallToRemoveDataFromCache(UserAuthModel user) {
        Long userId = userCacheService.removeUserNameFromRedisCache(user.getId());
        log.info("User Id removed from cache: {}", userId);
        String username = userCacheService.removeUserProfileDetailsFromRedisCache(user.getUsername());
        log.info("User profile details removed from cache with username: {}", username);
    }
}
