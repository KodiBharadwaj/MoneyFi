package com.moneyfi.apigateway.service.userservice;

import com.moneyfi.apigateway.dto.*;
import com.moneyfi.apigateway.model.auth.BlackListedToken;
import com.moneyfi.apigateway.model.auth.OtpTempModel;
import com.moneyfi.apigateway.model.auth.SessionTokenModel;
import com.moneyfi.apigateway.model.common.ProfileModel;
import com.moneyfi.apigateway.model.auth.UserAuthModel;
import com.moneyfi.apigateway.repository.auth.OtpTempRepository;
import com.moneyfi.apigateway.repository.common.ProfileRepository;
import com.moneyfi.apigateway.repository.auth.UserRepository;
import com.moneyfi.apigateway.service.TokenBlacklistService;
import com.moneyfi.apigateway.service.jwtservice.JwtService;
import com.moneyfi.apigateway.service.sessiontokens.SessionToken;
import com.moneyfi.apigateway.util.EmailFilter;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImplementation implements UserService {

    @Autowired
    private AuthenticationManager authenticationManager;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    private final UserRepository userRepository;
    private final EmailFilter emailFilter;
    private final RestTemplate restTemplate;
    private final OtpTempRepository otpTempRepository;
    private final JwtService jwtService;
    private final ProfileRepository profileRepository;
    private final SessionToken sessionTokenService;
    private final TokenBlacklistService blacklistService;

    public UserServiceImplementation(UserRepository userRepository,
                                     EmailFilter emailFilter,
                                     RestTemplate restTemplate,
                                     OtpTempRepository otpTempRepository,
                                     JwtService jwtService,
                                     ProfileRepository profileRepository,
                                     SessionToken sessionTokenService,
                                     TokenBlacklistService blacklistService){
        this.userRepository = userRepository;
        this.emailFilter = emailFilter;
        this.restTemplate = restTemplate;
        this.otpTempRepository = otpTempRepository;
        this.jwtService = jwtService;
        this.profileRepository = profileRepository;
        this.sessionTokenService = sessionTokenService;
        this.blacklistService = blacklistService;
    }

    @Override
    @Transactional
    public UserAuthModel registerUser(UserProfile userProfile) {
        UserAuthModel getUser = userRepository.findByUsername(userProfile.getUsername());
        if(getUser != null){
            return null;
        }

        UserAuthModel userAuthModel = new UserAuthModel();
        userAuthModel.setUsername(userProfile.getUsername());
        userAuthModel.setPassword(encoder.encode(userProfile.getPassword()));
        UserAuthModel user =  userRepository.save(userAuthModel);

        saveUserProfileDetails(user.getId(), userProfile);
        return user;
    }
    private void saveUserProfileDetails(Long userId, UserProfile userProfile){
        ProfileModel profile = new ProfileModel();
        profile.setUserId(userId);
        profile.setName(userProfile.getName());
        profile.setEmail(userProfile.getUsername());
        profile.setCreatedDate(LocalDate.now());
        profileRepository.save(profile);
    }

    @Override
    @Transactional
    public ResponseEntity<?> login(UserAuthModel userAuthModel) {

        makeOldSessionInActiveOfUserForNewLogin(userAuthModel);

        try {
            // Validate user input (username and password should not be empty)
            if (userAuthModel.getUsername() == null ||
                    userAuthModel.getUsername().isEmpty() ||
                    userAuthModel.getPassword() == null ||
                    userAuthModel.getPassword().isEmpty()) {

                return ResponseEntity.badRequest().body("Username and password are required");
            }
            // Check if the user exists in the database
            UserAuthModel existingUser = userRepository.findByUsername(userAuthModel.getUsername());
            if (existingUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("UserAuthModel not found. Please sign up.");
            }
            if(existingUser != null && existingUser.isDeleted()){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Account Blocked! Please contact admin");
            }

            try {
                // Authenticate the user with the provided password
                Authentication authentication = authenticationManager
                        .authenticate(new UsernamePasswordAuthenticationToken(userAuthModel.getUsername(), userAuthModel.getPassword()));

                if (authentication.isAuthenticated()) {
                    JwtToken token = jwtService.generateToken(userAuthModel.getUsername());
                    functionToPreventMultipleLogins(userAuthModel, token);
                    return ResponseEntity.ok(token);
                }
            } catch (BadCredentialsException ex) {
                // If the password is incorrect
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect password");
            }
            // Default case for any other authentication failures
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        } catch (Exception e) {
            // Handle any unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during login");
        }
    }
    private void makeOldSessionInActiveOfUserForNewLogin(UserAuthModel userAuthModel){

        SessionTokenModel sessionTokenUser = sessionTokenService.getUserByUsername(userAuthModel.getUsername());
        if(sessionTokenUser != null){
            if(sessionTokenUser.getIsActive()){
                String oldToken = sessionTokenUser.getToken();

                BlackListedToken blackListedToken = new BlackListedToken();
                blackListedToken.setToken(oldToken);
                Date expiryDate = new Date(System.currentTimeMillis() + 3600000);
                blackListedToken.setExpiry(expiryDate);
                blacklistService.blacklistToken(blackListedToken);
            }
        }
    }
    private void functionToPreventMultipleLogins(UserAuthModel userAuthModel, JwtToken token){
        // Conditions to store the jwt token to prevent multiple logins of same account in different browsers
        if(sessionTokenService.getUserByUsername(userAuthModel.getUsername()) != null){
            SessionTokenModel sessionTokens = sessionTokenService.getUserByUsername(userAuthModel.getUsername());
            sessionTokens.setUsername(userAuthModel.getUsername());
            sessionTokens.setCreatedTime(LocalDateTime.now());
            sessionTokens.setToken(token.getJwtToken());
            sessionTokens.setIsActive(true);
            sessionTokenService.save(sessionTokens);
        } else {
            SessionTokenModel sessionTokens = new SessionTokenModel();
            sessionTokens.setUsername(userAuthModel.getUsername());
            sessionTokens.setCreatedTime(LocalDateTime.now());
            sessionTokens.setToken(token.getJwtToken());
            sessionTokens.setIsActive(true);
            sessionTokenService.save(sessionTokens);
        }
    }

    @Override
    public Long getUserIdByUsername(String email) {
        UserAuthModel userAuthModel = userRepository.findByUsername(email);
        if(userAuthModel == null){
            return null;
        }

        return userAuthModel.getId();
    }

    @Override
    public ProfileChangePassword changePassword(ChangePasswordDto changePasswordDto){
        UserAuthModel userAuthModel = userRepository.findById(Long.valueOf(changePasswordDto.getUserId())).orElse(null);

        ProfileChangePassword dto = new ProfileChangePassword();
        if(userAuthModel == null) {
            dto.setFlag(false);
            return dto;
        }

        if(!encoder.matches(changePasswordDto.getCurrentPassword(), userAuthModel.getPassword())){
            dto.setFlag(false);
            return dto;
        }
        else if(userAuthModel.getOtpCount() >= 3){
            dto.setOtpCount(userAuthModel.getOtpCount());
            dto.setFlag(false);
            return dto;
        }

        new Thread(() -> sendPasswordAlertMail(userAuthModel.getId(), userAuthModel.getUsername())).start();

        userAuthModel.setPassword(encoder.encode(changePasswordDto.getNewPassword()));
        userAuthModel.setOtpCount(userAuthModel.getOtpCount()+1);
        userAuthModel.setVerificationCodeExpiration(LocalDateTime.now());
        userRepository.save(userAuthModel);

        dto.setFlag(true);
        return dto;
    }
    private void sendPasswordAlertMail(Long userId, String email){

        String userName = profileRepository.findByUserId(userId).getName();


        String subject = "Password Change Alert!!";
        String body = "<html>"
                + "<body>"
                + "<p style='font-size: 16px;'>Hello " + userName +",</p>"
                + "<p style='font-size: 16px;'>You have changed the password for your account with username: " + email + "</p>"
                + "<p style='font-size: 20px; font-weight: bold; color: #007BFF;'> </p>"
                + "<p style='font-size: 16px;'>Kindly Ignore if it by you. If not, reply to this mail immediately to secure account.</p>"
                + "<hr>"
                + "<p style='font-size: 14px; color: #555;'>If you have any issues, feel free to contact us at bharadwajkodi2003@gmail.com</p>"
                + "<br>"
                + "<p style='font-size: 14px;'>Best regards,</p>"
                + "<p style='font-size: 14px;'>The Support Team</p>"
                + "</body>"
                + "</html>";
        emailFilter.sendEmail(email, subject, body);
    }

    @Override
    public RemainingTimeCountDto checkOtpActiveMethod(String email){
        RemainingTimeCountDto remainingTimeCountDto = new RemainingTimeCountDto();

        UserAuthModel userAuthModel = userRepository.findByUsername(email);
        if(userAuthModel == null){
            remainingTimeCountDto.setComment("User not exist");
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

        UserAuthModel userData = userRepository.findByUsername(email);
        if(userData != null){
            return "User already exists!";
        }

        String verificationCode = emailFilter.generateVerificationCode();

        String subject = "OTP for MoneyFi's account creation";
        String body = "<html>"
                + "<body>"
                + "<p style='font-size: 16px;'>Hello " + name + ",</p>"
                + "<p style='font-size: 16px;'>You have requested for account creation. Please use the following verification code:</p>"
                + "<p style='font-size: 20px; font-weight: bold; color: #007BFF;'>" + verificationCode + "</p>"
                + "<p style='font-size: 16px;'>This code is valid for 5 minutes only. If you did not raise, please ignore this email.</p>"
                + "<hr>"
                + "<p style='font-size: 14px; color: #555;'>If you have any issues, feel free to contact us at bharadwajkodi2003@gmail.com</p>"
                + "<br>"
                + "<p style='font-size: 14px;'>Best regards,</p>"
                + "<p style='font-size: 14px;'>The Support Team</p>"
                + "</body>"
                + "</html>";
        boolean isMailsent = emailFilter.sendEmail(email, subject, body);

        if(isMailsent){
            OtpTempModel user = otpTempRepository.findByEmail(email);

            if(user != null){
                user.setOtp(verificationCode);
                user.setExpirationTime(LocalDateTime.now().plusMinutes(5));
                otpTempRepository.save(user);
            } else {
                OtpTempModel otpTempModel = new OtpTempModel();
                otpTempModel.setEmail(email);
                otpTempModel.setOtp(verificationCode);
                otpTempModel.setExpirationTime(LocalDateTime.now().plusMinutes(5));
                otpTempRepository.save(otpTempModel);
            }
            return "Email sent successfully!";

        } else {
            return "Cant send email!";
        }
    }

    @Override
    public boolean checkEnteredOtp(String email, String inputOtp) {
        OtpTempModel user = otpTempRepository.findByEmail(email);

        if(user == null){
            return false;
        }

        if(!user.getOtp().equals(inputOtp) || user.getExpirationTime().isBefore(LocalDateTime.now())){
            return false;
        }

        return true;
    }

    @Override
    @Transactional
    public Map<String, String> logout(String token) {
        Map<String, String> response = new HashMap<>();

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        BlackListedToken blackListedToken = makeUserTokenBlacklisted(token);
        SessionTokenModel sessionTokenModel = makeUserSessionInActive(token);

        if(blackListedToken != null && sessionTokenModel != null){
            response.put("message", "Logged out successfully");
        } else {
            response.put("message", "Logout failed!");
        }

        return response;
    }
    private BlackListedToken makeUserTokenBlacklisted(String token){

        Date expiryDate = new Date(System.currentTimeMillis()); // current date and time
        BlackListedToken blackListedToken = new BlackListedToken();
        blackListedToken.setToken(token);
        blackListedToken.setExpiry(expiryDate);
        return blacklistService.blacklistToken(blackListedToken);
    }
    private SessionTokenModel makeUserSessionInActive(String token){

        SessionTokenModel sessionTokens = sessionTokenService.getSessionDetailsByToken(token);
        sessionTokens.setIsActive(false);
        return sessionTokenService.save(sessionTokens);
    }


    @Scheduled(fixedRate = 3600000) // Runs every 1 hour
    public void removeOtpCountOfPreviousDay1(){
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        List<UserAuthModel>  userAuthModelList = userRepository.getUserListWhoseOtpCountGreaterThanThree(startOfToday);

        for (UserAuthModel userAuthModel : userAuthModelList) {
            userAuthModel.setOtpCount(0);
            userRepository.save(userAuthModel);
        }
    }

}
