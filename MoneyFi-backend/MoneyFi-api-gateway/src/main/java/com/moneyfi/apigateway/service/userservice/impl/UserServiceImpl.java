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
import com.moneyfi.apigateway.repository.user.ContactUsHistRepository;
import com.moneyfi.apigateway.repository.user.ContactUsRepository;
import com.moneyfi.apigateway.repository.user.auth.OtpTempRepository;
import com.moneyfi.apigateway.repository.user.ProfileRepository;
import com.moneyfi.apigateway.repository.user.auth.UserRepository;
import com.moneyfi.apigateway.service.common.AwsServices;
import com.moneyfi.apigateway.service.common.CloudinaryService;
import com.moneyfi.apigateway.service.common.UserCommonService;
import com.moneyfi.apigateway.service.userservice.UserService;
import com.moneyfi.apigateway.service.jwtservice.JwtService;
import com.moneyfi.apigateway.service.jwtservice.dto.JwtToken;
import com.moneyfi.apigateway.service.userservice.dto.request.AccountBlockRequestDto;
import com.moneyfi.apigateway.service.userservice.dto.request.ChangePasswordDto;
import com.moneyfi.apigateway.service.userservice.dto.request.ForgotUsernameDto;
import com.moneyfi.apigateway.service.userservice.dto.request.UserProfile;
import com.moneyfi.apigateway.service.userservice.dto.response.ProfileChangePassword;
import com.moneyfi.apigateway.service.userservice.dto.response.RemainingTimeCountDto;
import com.moneyfi.apigateway.util.EmailTemplates;
import com.moneyfi.apigateway.util.enums.OtpType;
import com.moneyfi.apigateway.util.enums.RaiseRequestStatus;
import com.moneyfi.apigateway.util.enums.RequestReason;
import com.moneyfi.apigateway.util.enums.UserRoles;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
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

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

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
                           @Autowired(required = false) CloudinaryService cloudinaryService){
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
    }

    @Override
    @Transactional
    public UserAuthModel registerUser(UserProfile userProfile) {
        UserAuthModel getUser = userRepository.getUserDetailsByUsername(userProfile.getUsername());
        if(getUser != null){
            return null;
        }

        UserAuthModel userAuthModel = new UserAuthModel();
        userAuthModel.setUsername(userProfile.getUsername());
        userAuthModel.setPassword(encoder.encode(userProfile.getPassword()));
        userAuthModel.setOtpCount(0);
        userAuthModel.setDeleted(false);
        userAuthModel.setBlocked(false);

        int roleId = 0;
        for(Map.Entry<Integer, String> it: userRoleAssociation.entrySet()){
            if(it.getValue().equalsIgnoreCase(userProfile.getRole())){
                roleId = it.getKey();
            }
        }
        userAuthModel.setRoleId(roleId);
        UserAuthModel user = userRepository.save(userAuthModel);

        if(!userRoleAssociation.get(roleId).equalsIgnoreCase(UserRoles.ADMIN.name())) {
            saveUserProfileDetails(user.getId(), userProfile);
            /** send successful email in a separate thread by aws ses **/
            new Thread(() -> {
                try {
                    awsServices.sendEmailToUserUsingAwsSes(emailTemplates.sendEmailForSuccessfulUserCreation(userProfile.getName(), userProfile.getUsername()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            ).start();
        }
        return user;
    }

    private void saveUserProfileDetails(Long userId, UserProfile userProfile){
        ProfileModel profile = new ProfileModel();
        profile.setUserId(userId);
        profile.setName(userProfile.getName());
        profile.setCreatedDate(LocalDateTime.now());
        profileRepository.save(profile);
    }

    @Override
    @Transactional
    public ResponseEntity<Map<String,String>> login(UserAuthModel userAuthModel) {
        Map<String,String> userRoleToken = new HashMap<>();

        makeOldSessionInActiveOfUserForNewLogin(userAuthModel);

        try {
            if (userAuthModel.getUsername() == null || userAuthModel.getUsername().isEmpty() ||
                    userAuthModel.getPassword() == null || userAuthModel.getPassword().isEmpty()) {
                userRoleToken.put(ERROR, USERNAME_PASSWORD_REQUIRED);
                return ResponseEntity.badRequest().body(userRoleToken);
            }

            UserAuthModel existingUser = userRepository.getUserDetailsByUsername(userAuthModel.getUsername());
            if (existingUser == null) {
                userRoleToken.put(ERROR, USER_NOT_FOUND);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(userRoleToken);
            } else if(existingUser.isBlocked()){
                userRoleToken.put(ERROR, ACCOUNT_BLOCKED);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(userRoleToken);
            } else if(existingUser.isDeleted()){
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

    private void makeOldSessionInActiveOfUserForNewLogin(UserAuthModel userAuthModel){

        SessionTokenModel sessionTokenUser = userCommonService.getUserByUsername(userAuthModel.getUsername());
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
    public Long getUserIdByUsername(String email) {
        UserAuthModel userAuthModel = userRepository.getUserDetailsByUsername(email);
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

        dto.setFlag(true);
        return dto;
    }

    @Override
    public RemainingTimeCountDto checkOtpActiveMethod(String email){
        RemainingTimeCountDto remainingTimeCountDto = new RemainingTimeCountDto();

        UserAuthModel userAuthModel = userRepository.getUserDetailsByUsername(email);
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

        UserAuthModel userData = userRepository.getUserDetailsByUsername(email);
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
            new Thread(()->
                    otpTempRepository.deleteByEmail(email)
            ).start(); return true;
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

        UserAuthModel user = userRepository.getUserDetailsByUsername(jwtService.extractUserName(token));
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
    public ResponseEntity<String> blockAccountByUserRequest(String username, AccountBlockRequestDto request) {
        if(request.getOtp() == null || request.getOtp().isEmpty() || request.getDescription() == null || request.getDescription().isEmpty()){
            throw new ScenarioNotPossibleException("Input fields should not be empty");
        }
        UserAuthModel user = userRepository.getUserDetailsByUsername(username);
        if(user == null){
            throw new ResourceNotFoundException("User not found");
        }
        if(user.isBlocked() || user.isDeleted()){
            throw new ScenarioNotPossibleException("User is not active to perform the operation");
        }

        List<OtpTempModel> userList = otpTempRepository.findByEmail(username);
        Optional<OtpTempModel> tempModel = userList
                .stream()
                .filter(tempOtp -> tempOtp.getOtpType().equalsIgnoreCase(OtpType.ACCOUNT_BLOCK.name()))
                .findFirst();

        if(tempModel.isPresent()){
            if(!tempModel.get().getOtp().equals(request.getOtp())){
                throw new ScenarioNotPossibleException("Please enter correct otp");
            }

            if(tempModel.get().getExpirationTime().isBefore(LocalDateTime.now())){
                throw new ScenarioNotPossibleException("Otp expired, Try new one");
            }

            ProfileModel userProfile = profileRepository.findByUserId(user.getId());
            String referenceNumber = "BL" + userProfile.getName().substring(0,2) + username.substring(0,2)
                    + (userProfile.getPhone() != null ? userProfile.getPhone().substring(0,2) + generateVerificationCode().substring(0,3) : generateVerificationCode());

            user.setBlocked(true);
            userRepository.save(user);

            ContactUs blockAccountRequest = new ContactUs();
            blockAccountRequest.setEmail(username);
            blockAccountRequest.setReferenceNumber(referenceNumber);
            blockAccountRequest.setRequestActive(true);
            blockAccountRequest.setRequestReason(RequestReason.ACCOUNT_BLOCK_REQUEST.name());
            blockAccountRequest.setVerified(false);
            blockAccountRequest.setRequestStatus(RaiseRequestStatus.SUBMITTED.name());
            blockAccountRequest.setStartTime(LocalDateTime.now());
            ContactUs savedRequest = contactUsRepository.save(blockAccountRequest);

            ContactUsHist blockAccountRequestHistory = new ContactUsHist();
            blockAccountRequestHistory.setName(userProfile.getName());
            blockAccountRequestHistory.setMessage(request.getDescription());
            blockAccountRequestHistory.setContactUsId(savedRequest.getId());
            blockAccountRequestHistory.setUpdatedTime(savedRequest.getStartTime());
            blockAccountRequestHistory.setRequestReason(RequestReason.ACCOUNT_BLOCK_REQUEST.name());
            blockAccountRequestHistory.setRequestStatus(RaiseRequestStatus.SUBMITTED.name());
            contactUsHistRepository.save(blockAccountRequestHistory);

            new Thread(
                    () -> {
                        otpTempRepository.deleteByEmail(username);
                        emailTemplates.sendReferenceNumberEmail(userProfile.getName(), username, "account block", referenceNumber);
                    }
            ).start();
            return ResponseEntity.ok("Account blocked successfully");
        } else {
            throw new ResourceNotFoundException("Otp request not found");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<String> sendOtpToBlockAccount(String username) {
        UserAuthModel userData = userRepository.getUserDetailsByUsername(username);
        if(userData == null){
            throw new ResourceNotFoundException("User not found");
        }
        if(userData.isBlocked() || userData.isDeleted()){
            throw new ScenarioNotPossibleException("You are not allowed to perform this operation");
        }

        String verificationCode = generateVerificationCode();
        boolean isMailsent = emailTemplates.sendOtpToUserForAccountBlock(username, profileRepository.findByUserId(userData.getId()).getName(), verificationCode);

        if(isMailsent){
            List<OtpTempModel> userList = otpTempRepository.findByEmail(username);
            Optional<OtpTempModel> tempModel = userList
                    .stream()
                    .filter(user -> user.getOtpType().equalsIgnoreCase(OtpType.ACCOUNT_BLOCK.name()))
                    .findFirst();

            if(tempModel.isPresent()){
                tempModel.get().setOtp(verificationCode);
                tempModel.get().setExpirationTime(LocalDateTime.now().plusMinutes(5));
                otpTempRepository.save(tempModel.get());
            } else {
                OtpTempModel otpTempModel = new OtpTempModel();
                otpTempModel.setEmail(username);
                otpTempModel.setOtp(verificationCode);
                otpTempModel.setExpirationTime(LocalDateTime.now().plusMinutes(5));
                otpTempModel.setOtpType(OtpType.ACCOUNT_BLOCK.name());
                otpTempRepository.save(otpTempModel);
            }

            return ResponseEntity.ok("Email sent successfully!");

        } else {
            return ResponseEntity.internalServerError().body("Cant send email!");
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
