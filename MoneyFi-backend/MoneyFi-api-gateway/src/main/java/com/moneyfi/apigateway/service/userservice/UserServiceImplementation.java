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
import com.moneyfi.apigateway.util.EmailTemplates;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class UserServiceImplementation implements UserService {

    private static final String MESSAGE = "message";

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    private final UserRepository userRepository;
    private final OtpTempRepository otpTempRepository;
    private final JwtService jwtService;
    private final ProfileRepository profileRepository;
    private final SessionToken sessionTokenService;
    private final TokenBlacklistService blacklistService;
    private AuthenticationManager authenticationManager;

    public UserServiceImplementation(UserRepository userRepository,
                                     OtpTempRepository otpTempRepository,
                                     JwtService jwtService,
                                     ProfileRepository profileRepository,
                                     SessionToken sessionTokenService,
                                     TokenBlacklistService blacklistService,
                                     AuthenticationManager authenticationManager){
        this.userRepository = userRepository;
        this.otpTempRepository = otpTempRepository;
        this.jwtService = jwtService;
        this.profileRepository = profileRepository;
        this.sessionTokenService = sessionTokenService;
        this.blacklistService = blacklistService;
        this.authenticationManager = authenticationManager;
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
        userAuthModel.setOtpCount(0);
        userAuthModel.setDeleted(false);
        userAuthModel.setBlocked(false);
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
            else if(existingUser.isBlocked()){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Account Blocked! Please contact admin");
            }
            else if(existingUser.isDeleted()){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Account Deleted! Please contact admin");
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
        if(sessionTokenUser != null && sessionTokenUser.getIsActive()){
            String oldToken = sessionTokenUser.getToken();

            BlackListedToken blackListedToken = new BlackListedToken();
            blackListedToken.setToken(oldToken);
            Date expiryDate = new Date(System.currentTimeMillis() + 3600000);
            blackListedToken.setExpiry(expiryDate);
            blacklistService.blacklistToken(blackListedToken);
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
        UserAuthModel userAuthModel = userRepository.findById(changePasswordDto.getUserId()).orElse(null);

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

        String userName = profileRepository.findByUserId(userAuthModel.getId()).getName();
        new Thread(() ->
                EmailTemplates.sendPasswordAlertMail(userName, userAuthModel.getUsername())
        ).start();

        userAuthModel.setPassword(encoder.encode(changePasswordDto.getNewPassword()));
        userAuthModel.setOtpCount(userAuthModel.getOtpCount()+1);
        userAuthModel.setVerificationCodeExpiration(LocalDateTime.now());
        userRepository.save(userAuthModel);

        dto.setFlag(true);
        return dto;
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

        String verificationCode = EmailFilter.generateVerificationCode();
        boolean isMailsent = EmailTemplates.sendEmailToUserForSignup(email, name, verificationCode);

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

        return !(user == null || !user.getOtp().equals(inputOtp) ||
                user.getExpirationTime().isBefore(LocalDateTime.now()));
    }

    @Override
    @Transactional
    public Map<String, String> logout(String token) {
        Map<String, String> response = new HashMap<>();

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Long userId = userRepository.findByUsername(jwtService.extractUserName(token)).getId();
        String phoneNumber = profileRepository.findByUserId(userId).getPhone();

        if(phoneNumber == null || phoneNumber.isEmpty()){
            response.put(MESSAGE, "Phone number is empty");
            return response;
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
        return blacklistService.blacklistToken(blackListedToken);
    }
    private SessionTokenModel makeUserSessionInActive(String token){

        SessionTokenModel sessionTokens = sessionTokenService.getSessionDetailsByToken(token);
        sessionTokens.setIsActive(false);
        return sessionTokenService.save(sessionTokens);
    }


    @Override
    public boolean getUsernameByDetails(ForgotUsernameDto userDetails) {
        String username = functionCallToRetrieveUsername(userDetails);

        if(username == null || username.trim().isEmpty()){
            return false;
        }

        log.info("Username fetched: {}", username);
        return EmailTemplates.sendUserNameToUser(username);
    }
    private String functionCallToRetrieveUsername(ForgotUsernameDto userDetails){
        String username = "";

        if(userDetails.getPhoneNumber() != null && !userDetails.getPhoneNumber().isEmpty()
                && userDetails.getPhoneNumber().length() == 10){

            List<ProfileModel> fetchedUsers = profileRepository.findByPhone(userDetails.getPhoneNumber());

            if(fetchedUsers.size() == 1){
                return fetchedUsers.get(0).getEmail();
            }

            fetchedUsers = fetchedUsers
                    .stream()
                    .filter(user -> user.getDateOfBirth().equals(userDetails.getDateOfBirth()))
                    .toList();
            if(fetchedUsers.size() == 1){
                return fetchedUsers.get(0).getEmail();
            }

            fetchedUsers = fetchedUsers
                    .stream()
                    .filter(user -> user.getName().equalsIgnoreCase(userDetails.getName()))
                    .toList();
            if(fetchedUsers.size() == 1){
                return fetchedUsers.get(0).getEmail();
            }

            fetchedUsers = fetchedUsers
                    .stream()
                    .filter(user -> user.getGender().equalsIgnoreCase(userDetails.getGender())
                            && user.getMaritalStatus().equalsIgnoreCase(userDetails.getMaritalStatus()))
                    .toList();
            if(fetchedUsers.size() == 1){
                return fetchedUsers.get(0).getEmail();
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
                        matchedUsernames.add(profile.getEmail());
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
                return fetchedUsersByAllDetails.get(0).getEmail();
            }

            List<String> matchedUsernames = functionToFetchUserByPinCode(fetchedUsersByAllDetails, userDetails);
            if(matchedUsernames.size() == 1){
                return matchedUsernames.get(0);
            }
        }
        return null;
    }




    @Scheduled(fixedRate = 3600000) // Method Runs for every 1 hour
    public void removeOtpCountOfPreviousDay1(){
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        List<UserAuthModel>  userAuthModelList = userRepository.getUserListWhoseOtpCountGreaterThanThree(startOfToday);

        for (UserAuthModel userAuthModel : userAuthModelList) {
            userAuthModel.setOtpCount(0);
            userRepository.save(userAuthModel);
        }
    }

}
