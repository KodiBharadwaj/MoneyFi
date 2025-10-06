package com.moneyfi.apigateway.service.userservice.impl;

import com.moneyfi.apigateway.exceptions.ResourceNotFoundException;
import com.moneyfi.apigateway.exceptions.ScenarioNotPossibleException;
import com.moneyfi.apigateway.model.auth.BlackListedToken;
import com.moneyfi.apigateway.model.auth.OtpTempModel;
import com.moneyfi.apigateway.model.auth.SessionTokenModel;
import com.moneyfi.apigateway.model.common.ContactUs;
import com.moneyfi.apigateway.model.common.ContactUsHist;
import com.moneyfi.apigateway.model.common.ProfileModel;
import com.moneyfi.apigateway.model.auth.UserAuthModel;
import com.moneyfi.apigateway.model.common.UserAuthHist;
import com.moneyfi.apigateway.repository.user.ContactUsHistRepository;
import com.moneyfi.apigateway.repository.user.ContactUsRepository;
import com.moneyfi.apigateway.repository.user.UserAuthHistRepository;
import com.moneyfi.apigateway.repository.user.auth.OtpTempRepository;
import com.moneyfi.apigateway.repository.user.ProfileRepository;
import com.moneyfi.apigateway.repository.user.auth.UserRepository;
import com.moneyfi.apigateway.service.common.AwsServices;
import com.moneyfi.apigateway.service.common.CloudinaryService;
import com.moneyfi.apigateway.service.common.UserCommonService;
import com.moneyfi.apigateway.service.userservice.UserService;
import com.moneyfi.apigateway.service.jwtservice.JwtService;
import com.moneyfi.apigateway.service.jwtservice.dto.JwtToken;
import com.moneyfi.apigateway.service.userservice.dto.request.AccountBlockOrDeleteRequestDto;
import com.moneyfi.apigateway.service.userservice.dto.request.ChangePasswordDto;
import com.moneyfi.apigateway.service.userservice.dto.request.ForgotUsernameDto;
import com.moneyfi.apigateway.service.userservice.dto.request.UserProfile;
import com.moneyfi.apigateway.service.userservice.dto.response.ProfileChangePassword;
import com.moneyfi.apigateway.service.userservice.dto.response.RemainingTimeCountDto;
import com.moneyfi.apigateway.util.EmailTemplates;
import com.moneyfi.apigateway.util.constants.StringUtils;
import com.moneyfi.apigateway.util.enums.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    private static final String GOOGLE_TOKEN_END_POINT_URL = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_USER_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";
    private static final String GOOGLE_AUTH_CONSTANT_PASSWORD = "123456";
    private static final String GITHUB_TOKEN_END_POINT_URL = "https://github.com/login/oauth/access_token";
    private static final String GITHUB_USER_URL = "https://api.github.com/user";
    private static final String GITHUB_USER_EMAILS = "https://api.github.com/user/emails";


    private final UserRepository userRepository;
    private final OtpTempRepository otpTempRepository;
    private final JwtService jwtService;
    private final ProfileRepository profileRepository;
    private final UserCommonService userCommonService;
    private final ContactUsRepository contactUsRepository;
    private final ContactUsHistRepository contactUsHistRepository;
    private final EmailTemplates emailTemplates;
    private final AwsServices awsServices;
    private final CloudinaryService cloudinaryService;
    private final UserAuthHistRepository userAuthHistRepository;
    private final AuthenticationManager authenticationManager;

    public UserServiceImpl(UserRepository userRepository,
                           OtpTempRepository otpTempRepository,
                           JwtService jwtService,
                           ProfileRepository profileRepository,
                           UserCommonService userCommonService,
                           ContactUsRepository contactUsRepository,
                           ContactUsHistRepository contactUsHistRepository,
                           AuthenticationManager authenticationManager,
                           EmailTemplates emailTemplates,
                           AwsServices awsServices,
                           @Autowired(required = false) CloudinaryService cloudinaryService,
                           UserAuthHistRepository userAuthHistRepository){
        this.userRepository = userRepository;
        this.otpTempRepository = otpTempRepository;
        this.jwtService = jwtService;
        this.profileRepository = profileRepository;
        this.userCommonService = userCommonService;
        this.contactUsRepository = contactUsRepository;
        this.contactUsHistRepository = contactUsHistRepository;
        this.authenticationManager = authenticationManager;
        this.emailTemplates = emailTemplates;
        this.awsServices = awsServices;
        this.cloudinaryService = cloudinaryService;
        this.userAuthHistRepository = userAuthHistRepository;
    }

    @Override
    @Transactional
    public UserAuthModel registerUser(UserProfile userProfile, String loginMode, String address) {
        UserAuthModel getUser = userRepository.getUserDetailsByUsername(userProfile.getUsername()).orElse(null);
        if(getUser != null){
            return null;
        }
        UserAuthModel userAuthModel = new UserAuthModel();
        saveUserAuthDetails(userAuthModel, userProfile.getUsername(), encoder.encode(userProfile.getPassword()));

        int roleId = 0;
        if (loginMode.equalsIgnoreCase(LoginMode.EMAIL_PASSWORD.name())) {
            for (Map.Entry<Integer, String> it : userRoleAssociation.entrySet()) {
                if (it.getValue().equalsIgnoreCase(userProfile.getRole())) {
                    roleId = it.getKey();
                }
            }
            userAuthModel.setLoginCodeValue(LoginMode.EMAIL_PASSWORD.getLoginProcessCode());
        } else if (loginMode.equalsIgnoreCase(LoginMode.GOOGLE_OAUTH.name())) {
            for (Map.Entry<Integer, String> it : userRoleAssociation.entrySet()) {
                if (it.getValue().equalsIgnoreCase(UserRoles.USER.name())) {
                    roleId = it.getKey();
                }
            }
            userAuthModel.setLoginCodeValue(LoginMode.GOOGLE_OAUTH.getLoginProcessCode());
        } else if (loginMode.equalsIgnoreCase(LoginMode.GITHUB_OAUTH.name())) {
            for (Map.Entry<Integer, String> it : userRoleAssociation.entrySet()) {
                if (it.getValue().equalsIgnoreCase(UserRoles.USER.name())) {
                    roleId = it.getKey();
                }
            }
            userAuthModel.setLoginCodeValue(LoginMode.GITHUB_OAUTH.getLoginProcessCode());
        }
        userAuthModel.setRoleId(roleId);
        UserAuthModel user = userRepository.save(userAuthModel);

        if(!userRoleAssociation.get(user.getRoleId()).equalsIgnoreCase(UserRoles.ADMIN.name())) {
            saveUserProfileDetails(user.getId(), userProfile, address);
            /** send successful email in a separate thread by aws ses **/
            new Thread(() -> {
                try {
                    awsServices.sendEmailToUserUsingAwsSes(emailTemplates.sendEmailForSuccessfulUserCreation(userProfile.getName(), userProfile.getUsername()));
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException("Exception occurred while sending email: " + e);
                }
            }
            ).start();
        }
        return user;
    }

    private void saveUserAuthDetails(UserAuthModel userAuthModel, String username, String password){
        userAuthModel.setUsername(username);
        userAuthModel.setPassword(password);
        userAuthModel.setOtpCount(0);
        userAuthModel.setDeleted(false);
        userAuthModel.setBlocked(false);
    }

    private void saveUserProfileDetails(Long userId, UserProfile userProfile, String address){
        ProfileModel profile = new ProfileModel();
        profile.setUserId(userId);
        profile.setName(userProfile.getName());
        profile.setCreatedDate(LocalDateTime.now());
        if(address != null && !address.isEmpty()) profile.setAddress(address);
        profileRepository.save(profile);
    }

    @Override
    @Transactional
    public ResponseEntity<Map<String,String>> login(UserAuthModel userAuthModel) {
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
                userRoleToken.put(ERROR, USER_NOT_FOUND);
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
        // Conditions to store the jwt token to prevent multiple logins of same account in different browsers
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
                    if(picture != null && !picture.trim().isEmpty()) uploadUserProfilePictureToS3(email, StringUtils.convertImageUrlToMultipartFile(picture));
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
                user = registerUser(new UserProfile((name != null && !name.trim().isEmpty()) ? name : "Github User", email, GOOGLE_AUTH_CONSTANT_PASSWORD, UserRoles.USER.name()),
                        LoginMode.GITHUB_OAUTH.name(), (address != null && !address.trim().isEmpty()) ? address : null);
                if(picture != null && !picture.trim().isEmpty()) uploadUserProfilePictureToS3(email, StringUtils.convertImageUrlToMultipartFile(picture));
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
        if(userAuthModel == null){
            return null;
        }
        return userAuthModel.getId();
    }

    @Override
    @Transactional
    public ProfileChangePassword changePassword(ChangePasswordDto changePasswordDto){
        UserAuthModel userAuthModel = userRepository.findById(changePasswordDto.getUserId()).orElse(null);
        if(changePasswordDto.getDescription() == null || changePasswordDto.getDescription().trim().isEmpty()){
            throw new ScenarioNotPossibleException("Description is mandatory");
        }
        ProfileChangePassword dto = new ProfileChangePassword();
        if(userAuthModel == null) {
            dto.setFlag(false);
            return dto;
        }
        if(!encoder.matches(changePasswordDto.getCurrentPassword(), userAuthModel.getPassword())){
            dto.setFlag(false);
            return dto;
        } else if(userAuthModel.getOtpCount() >= 3){
            dto.setOtpCount(userAuthModel.getOtpCount());
            dto.setFlag(false);
            return dto;
        }
        String userName = profileRepository.findByUserId(userAuthModel.getId()).getName();
        new Thread(() ->
                emailTemplates.sendPasswordChangeAlertMail(userName, userAuthModel.getUsername())
        ).start();

        userAuthModel.setPassword(encoder.encode(changePasswordDto.getNewPassword()));
        userAuthModel.setOtpCount(userAuthModel.getOtpCount()+1);
        userAuthModel.setVerificationCodeExpiration(LocalDateTime.now());
        userRepository.save(userAuthModel);

        UserAuthHist userAuthHist = new UserAuthHist();
        userAuthHist.setUserId(changePasswordDto.getUserId());
        userAuthHist.setComment(changePasswordDto.getDescription());
        userAuthHist.setReasonTypeId(reasonCodeIdAssociation.get(ReasonEnum.BLOCK_ACCOUNT));
        userAuthHist.setUpdatedTime(LocalDateTime.now());
        userAuthHist.setUpdatedBy(changePasswordDto.getUserId());
        userAuthHistRepository.save(userAuthHist);

        dto.setFlag(true);
        return dto;
    }

    @Override
    public RemainingTimeCountDto checkOtpActiveMethod(String email){
        RemainingTimeCountDto remainingTimeCountDto = new RemainingTimeCountDto();
        UserAuthModel userAuthModel = userRepository.getUserDetailsByUsername(email).orElse(null);
        if(userAuthModel == null){
            remainingTimeCountDto.setComment("User not exist");
            remainingTimeCountDto.setResult(false);
            return remainingTimeCountDto;
        } else if(userAuthModel.isBlocked()){
            remainingTimeCountDto.setComment("Account Blocked! Please contact admin");
            remainingTimeCountDto.setResult(false);
            return remainingTimeCountDto;
        } else if(userAuthModel.isDeleted()){
            remainingTimeCountDto.setComment("Account Deleted! Please contact admin");
            remainingTimeCountDto.setResult(false);
            return remainingTimeCountDto;
        }

        if(userAuthModel.getOtpCount() >= 3){
            remainingTimeCountDto.setResult(false);
            remainingTimeCountDto.setComment("Limit crossed for today!! Try tomorrow");
            return remainingTimeCountDto;
        }

        if(userAuthModel.getVerificationCodeExpiration() == null || userAuthModel.getVerificationCodeExpiration().isBefore(LocalDateTime.now())){
            remainingTimeCountDto.setResult(true);
            return remainingTimeCountDto;
        }

        LocalDateTime time1 = LocalDateTime.now();
        LocalDateTime time2 = userAuthModel.getVerificationCodeExpiration();
        long minutesDifference = ChronoUnit.MINUTES.between(time1, time2);
        remainingTimeCountDto.setRemainingMinutes((int) minutesDifference);
        remainingTimeCountDto.setResult(false);
        return remainingTimeCountDto;
    }

    @Override
    public String sendOtpForSignup(String email, String name) {

        UserAuthModel userData = userRepository.getUserDetailsByUsername(email).orElse(null);
        if(userData != null){
            return "User already exists!";
        }

        String verificationCode = generateVerificationCode();
        boolean isMailsent = emailTemplates.sendOtpEmailToUserForSignup(email, name, verificationCode);

        if(isMailsent){
            List<OtpTempModel> userList = otpTempRepository.findByEmail(email);
            Optional<OtpTempModel> tempModel = userList
                    .stream()
                    .filter(tempOtp -> tempOtp.getOtpType().equalsIgnoreCase(OtpType.USER_CREATION.name()))
                    .findFirst();

            if(tempModel.isPresent()){
                tempModel.get().setOtp(verificationCode);
                tempModel.get().setExpirationTime(LocalDateTime.now().plusMinutes(5));
                otpTempRepository.save(tempModel.get());
            } else {
                OtpTempModel otpTempModel = new OtpTempModel();
                otpTempModel.setEmail(email);
                otpTempModel.setOtp(verificationCode);
                otpTempModel.setExpirationTime(LocalDateTime.now().plusMinutes(5));
                otpTempModel.setOtpType(OtpType.USER_CREATION.name());
                otpTempRepository.save(otpTempModel);
            }
            return "Email sent successfully!";

        } else {
            return "Cant send email!";
        }
    }

    @Override
    public boolean checkEnteredOtp(String email, String inputOtp) {
        List<OtpTempModel> userList = otpTempRepository.findByEmail(email);
        Optional<OtpTempModel> tempModel = userList
                .stream()
                .filter(tempOtp -> tempOtp.getOtpType().equalsIgnoreCase(OtpType.USER_CREATION.name()))
                .findFirst();

        if(tempModel.isPresent()){
            new Thread(()-> {
                int rowsAffected = otpTempRepository.deleteByEmailAndRequestType(email, OtpType.USER_CREATION.name());
                log.info("Number of OTP records deleted: {}", rowsAffected);
            }).start(); return true;
        } else return !(!tempModel.get().getOtp().equals(inputOtp) ||
                tempModel.get().getExpirationTime().isBefore(LocalDateTime.now()));
    }

    @Override
    @Transactional
    public Map<String, String> logout(String token) {
        Map<String, String> response = new HashMap<>();

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        UserAuthModel user = userRepository.getUserDetailsByUsername(jwtService.extractUserName(token)).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if(userRoleAssociation.get(user.getRoleId()).equalsIgnoreCase("USER")){
            String phoneNumber = profileRepository.findByUserId(user.getId()).getPhone();
            if(phoneNumber == null || phoneNumber.isEmpty()){
                response.put(MESSAGE, "Phone number is empty");
                return response;
            }
        }

        BlackListedToken blackListedToken = makeUserTokenBlacklisted(token);
        SessionTokenModel sessionTokenModel = makeUserSessionInActive(token);

        if(blackListedToken != null && sessionTokenModel != null){
            response.put(MESSAGE, "Logged out successfully");
        }
        else {
            response.put(MESSAGE, "Logout failed!");
        }

        return response;
    }

    private BlackListedToken makeUserTokenBlacklisted(String token){

        Date expiryDate = new Date(System.currentTimeMillis()); // current date and time
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
    public boolean getUsernameByDetails(ForgotUsernameDto userDetails) {
        String username = functionCallToRetrieveUsername(userDetails);

        if(username == null || username.trim().isEmpty()){
            return false;
        }

        log.info("Username fetched: {}", username);
        return emailTemplates.sendUserNameToUser(username);
    }

    @Override
    public boolean sendAccountStatementEmail(String username, byte[] pdfBytes) {
        return emailTemplates.sendAccountStatementAsEmail(profileRepository.findByUserId(getUserIdByUsername(username)).getName(), username, pdfBytes);
    }

    @Override
    public boolean sendSpendingAnalysisEmail(String username, byte[] pdfBytes) {
        return emailTemplates.sendSpendingAnalysisEmail(profileRepository.findByUserId(getUserIdByUsername(username)).getName(), username, pdfBytes);
    }

    @Override
    public String uploadUserProfilePictureToS3(String username, MultipartFile file) {
        if ("local".equalsIgnoreCase(activeProfile)) {
            cloudinaryService.uploadProfilePictureToCloudinary(file, getUserIdByUsername(username), username);
            return "Upload Successful";
        } else {
            return awsServices.uploadUserProfilePictureToS3(getUserIdByUsername(username), username, file);
        }
    }

    @Override
    public ResponseEntity<ByteArrayResource> fetchUserProfilePictureFromS3(String username) {
        if ("local".equalsIgnoreCase(activeProfile)) {
            byte[] imageBytes = cloudinaryService.getUserProfileFromCloudinary(getUserIdByUsername(username), username);
            ByteArrayResource resource = new ByteArrayResource(imageBytes);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .contentLength(imageBytes.length)
                    .body(resource);
        } else {
            return awsServices.fetchUserProfilePictureFromS3(getUserIdByUsername(username), username);
        }
    }

    @Override
    public ResponseEntity<String> deleteProfilePictureFromS3(String username) {
        if ("local".equalsIgnoreCase(activeProfile)) {
            return cloudinaryService.deleteProfilePictureFromCloudinary(getUserIdByUsername(username), username);
        } else {
            return awsServices.deleteProfilePictureFromS3(getUserIdByUsername(username), username);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<String> blockOrDeleteAccountByUserRequest(String username, AccountBlockOrDeleteRequestDto request) {
        if(request.getOtp() == null || request.getOtp().isEmpty() || request.getDescription() == null || request.getDescription().isEmpty()){
            throw new ScenarioNotPossibleException("Input fields should not be empty");
        }
        UserAuthModel user = userRepository.getUserDetailsByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if(user.isBlocked() || user.isDeleted()){
            throw new ScenarioNotPossibleException("User is not active to perform the operation");
        }

        String deactivationType; String referencePrefix;
        if(request.getDeactivationType().equalsIgnoreCase(AccDeactivationType.BLOCK.name())){
            deactivationType = OtpType.ACCOUNT_BLOCK.name();
            referencePrefix = "BL";
        } else if(request.getDeactivationType().equalsIgnoreCase(AccDeactivationType.DELETE.name())){
            deactivationType = OtpType.ACCOUNT_DELETE.name();
            referencePrefix = "DL";
        } else {
            throw new ScenarioNotPossibleException("Invalid deactivation type");
        }
        Optional<OtpTempModel> tempModel = otpTempRepository.findByEmail(username)
                .stream()
                .filter(tempOtp -> tempOtp.getOtpType().equalsIgnoreCase(deactivationType) && tempOtp.getExpirationTime().isAfter(LocalDateTime.now()))
                .findFirst();
        if(tempModel.isPresent()){
            OtpTempModel response = tempModel.get();
            if(!response.getOtp().equals(request.getOtp())){
                throw new ScenarioNotPossibleException("Please enter correct otp");
            }
            if(response.getExpirationTime().isBefore(LocalDateTime.now())){
                throw new ScenarioNotPossibleException("Otp expired, Try new one");
            }
            if(request.getDeactivationType().equalsIgnoreCase(AccDeactivationType.DELETE.name())){
                String userPassword = request.getPassword();
                if(userPassword == null || userPassword.isEmpty() || !encoder.matches(userPassword, user.getPassword())){
                    throw new ScenarioNotPossibleException("Incorrect Password entered!");
                }
            }
            ProfileModel userProfile = profileRepository.findByUserId(user.getId());
            String referenceNumber = referencePrefix + userProfile.getName().substring(0,2) + username.substring(0,2)
                    + (userProfile.getPhone() != null ? userProfile.getPhone().substring(0,2) + generateVerificationCode().substring(0,3) : generateVerificationCode());

            ContactUs accountDeactivationRequest = new ContactUs();
            UserAuthHist userAuthHist = new UserAuthHist();
            ContactUsHist blockAccountOrDeleteRequestHistory = new ContactUsHist();
            if (deactivationType.equalsIgnoreCase(OtpType.ACCOUNT_BLOCK.name())) {
                user.setBlocked(true);
                accountDeactivationRequest.setRequestReason(RequestReason.ACCOUNT_BLOCK_REQUEST.name());
                userAuthHist.setReasonTypeId(reasonCodeIdAssociation.get(ReasonEnum.BLOCK_ACCOUNT));
                blockAccountOrDeleteRequestHistory.setMessage(BLOCKED_BY_USER + ", " + request.getDescription());
            } else {
                user.setDeleted(true);
                accountDeactivationRequest.setRequestReason(RequestReason.ACCOUNT_DELETE_REQUEST.name());
                userAuthHist.setReasonTypeId(reasonCodeIdAssociation.get(ReasonEnum.DELETE_ACCOUNT));
                blockAccountOrDeleteRequestHistory.setMessage("Deleted by User," + request.getDescription());
            }
            userRepository.save(user);
            accountDeactivationRequest.setEmail(username);
            accountDeactivationRequest.setReferenceNumber(referenceNumber);
            accountDeactivationRequest.setRequestActive(true);
            accountDeactivationRequest.setVerified(false);
            accountDeactivationRequest.setRequestStatus(RaiseRequestStatus.SUBMITTED.name());
            accountDeactivationRequest.setStartTime(LocalDateTime.now());
            ContactUs savedRequest = contactUsRepository.save(accountDeactivationRequest);

            blockAccountOrDeleteRequestHistory.setName(userProfile.getName());
            blockAccountOrDeleteRequestHistory.setContactUsId(savedRequest.getId());
            blockAccountOrDeleteRequestHistory.setUpdatedTime(savedRequest.getStartTime());
            blockAccountOrDeleteRequestHistory.setRequestReason(savedRequest.getRequestReason());
            blockAccountOrDeleteRequestHistory.setRequestStatus(savedRequest.getRequestStatus());
            contactUsHistRepository.save(blockAccountOrDeleteRequestHistory);

            userAuthHist.setUserId(user.getId());
            userAuthHist.setComment(request.getDescription());
            userAuthHist.setUpdatedBy(user.getId());
            userAuthHist.setUpdatedTime(savedRequest.getStartTime());
            userAuthHistRepository.save(userAuthHist);
            new Thread(
                    () -> {
                        int rowsAffected = otpTempRepository.deleteByEmailAndRequestType(username, deactivationType);
                        log.info("Number of OTP entries deleted: {}", rowsAffected);
                        /** emailTemplates.sendReferenceNumberEmail(userProfile.getName(), username, "account block", referenceNumber); **/
                    }
            ).start();
            return ResponseEntity.ok("Account " + (request.getDeactivationType().equalsIgnoreCase(AccDeactivationType.BLOCK.name()) ? "Blocked" : "Deleted") + " successfully");
        } else {
            throw new ResourceNotFoundException("Otp request not found");
        }
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public ResponseEntity<String> sendOtpToBlockAccount(String username, String type) {
        UserAuthModel userData = userRepository.getUserDetailsByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User not found with username " + username));
        if(userData.isBlocked() || userData.isDeleted()){
            throw new ScenarioNotPossibleException("You are not allowed to perform this operation");
        }
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
                    String newOtp = StringUtils.generateVerificationCode();
                    otpTempRepository.save(new OtpTempModel(username, newOtp, LocalDateTime.now().plusMinutes(5), otpType));
                    return newOtp;
                });
        if (emailTemplates.sendOtpToUserForAccountBlock(username, profileRepository.findByUserId(userData.getId()).getName(), verificationCode, type)) {
            return ResponseEntity.ok("Email sent successfully!");
        } else {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Can't send email!");
        }
    }

    private String functionCallToRetrieveUsername(ForgotUsernameDto userDetails){
        String username = "";

        if(userDetails.getPhoneNumber() != null && !userDetails.getPhoneNumber().isEmpty()
                && userDetails.getPhoneNumber().length() == 10){

            List<ProfileModel> fetchedUsers = profileRepository.findByPhone(userDetails.getPhoneNumber());

            if(fetchedUsers.size() == 1){
                return userRepository.findById(fetchedUsers.get(0).getUserId()).get().getUsername();
            }

            fetchedUsers = fetchedUsers
                    .stream()
                    .filter(user -> user.getDateOfBirth().equals(userDetails.getDateOfBirth()))
                    .toList();
            if(fetchedUsers.size() == 1){
                return userRepository.findById(fetchedUsers.get(0).getUserId()).get().getUsername();
            }

            fetchedUsers = fetchedUsers
                    .stream()
                    .filter(user -> user.getName().equalsIgnoreCase(userDetails.getName()))
                    .toList();
            if(fetchedUsers.size() == 1){
                return userRepository.findById(fetchedUsers.get(0).getUserId()).get().getUsername();
            }

            fetchedUsers = fetchedUsers
                    .stream()
                    .filter(user -> user.getGender().equalsIgnoreCase(userDetails.getGender())
                            && user.getMaritalStatus().equalsIgnoreCase(userDetails.getMaritalStatus()))
                    .toList();
            if(fetchedUsers.size() == 1){
                return userRepository.findById(fetchedUsers.get(0).getUserId()).get().getUsername();
            }

            List<String> matchedUsernames = functionToFetchUserByPinCode(fetchedUsers, userDetails);
            if(matchedUsernames.size() == 1){
                return matchedUsernames.get(0);
            }

            username += "null";
        }

        return functionCallToFetchUsernameByUserDetailsWithoutPhoneNumber(username, userDetails);
    }

    private List<String> functionToFetchUserByPinCode(List<ProfileModel> fetchedUsers, ForgotUsernameDto userDetails){
        List<String> matchedUsernames = new ArrayList<>();

        for(ProfileModel profile : fetchedUsers){

            String address = profile.getAddress();
            if (address != null && !address.isEmpty()) {
                Pattern pattern = Pattern.compile("\\b\\d{6}\\b");
                Matcher matcher = pattern.matcher(address);

                String pincode = null;
                if (matcher.find()) {
                    pincode = matcher.group();

                    if (pincode.equals(userDetails.getPinCode())) {
                        matchedUsernames.add(userRepository.findById(profile.getUserId()).get().getUsername());
                    }
                }
            }
        }
        return matchedUsernames;
    }

    private String functionCallToFetchUsernameByUserDetailsWithoutPhoneNumber(String username, ForgotUsernameDto userDetails){

        if(username.isEmpty() || username.equalsIgnoreCase("null")){

            List<ProfileModel> fetchedUsersByAllDetails = profileRepository
                    .findByUserProfileDetails(userDetails.getDateOfBirth(), userDetails.getName(), userDetails.getGender(),
                            userDetails.getMaritalStatus());

            if(fetchedUsersByAllDetails.size() == 1){
                return userRepository.findById(fetchedUsersByAllDetails.get(0).getUserId()).get().getUsername();
            }

            List<String> matchedUsernames = functionToFetchUserByPinCode(fetchedUsersByAllDetails, userDetails);
            if(matchedUsernames.size() == 1){
                return matchedUsernames.get(0);
            }
        }
        return null;
    }
}
