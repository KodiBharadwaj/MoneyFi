package com.moneyfi.apigateway.service.userservice.impl;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.*;
import com.amazonaws.util.IOUtils;
import com.moneyfi.apigateway.exceptions.S3AwsErrorThrowException;
import com.moneyfi.apigateway.model.auth.BlackListedToken;
import com.moneyfi.apigateway.model.auth.OtpTempModel;
import com.moneyfi.apigateway.model.auth.SessionTokenModel;
import com.moneyfi.apigateway.model.common.ProfileModel;
import com.moneyfi.apigateway.model.auth.UserAuthModel;
import com.moneyfi.apigateway.repository.user.auth.OtpTempRepository;
import com.moneyfi.apigateway.repository.user.ProfileRepository;
import com.moneyfi.apigateway.repository.user.auth.UserRepository;
import com.moneyfi.apigateway.service.common.UserCommonService;
import com.moneyfi.apigateway.service.userservice.UserService;
import com.moneyfi.apigateway.service.userservice.dto.*;
import com.moneyfi.apigateway.service.jwtservice.JwtService;
import com.moneyfi.apigateway.service.jwtservice.dto.JwtToken;
import com.moneyfi.apigateway.util.EmailFilter;
import com.moneyfi.apigateway.util.EmailTemplates;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.moneyfi.apigateway.util.constants.StringUtils.*;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Value("${application.bucket.name}")
    private String bucketName;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    private final UserRepository userRepository;
    private final OtpTempRepository otpTempRepository;
    private final JwtService jwtService;
    private final ProfileRepository profileRepository;
    private final UserCommonService userCommonService;

    private final AuthenticationManager authenticationManager;
    private final AmazonS3 s3Client;
    private final AmazonSimpleEmailService amazonSimpleEmailService;

    public UserServiceImpl(UserRepository userRepository,
                           OtpTempRepository otpTempRepository,
                           JwtService jwtService,
                           ProfileRepository profileRepository,
                           UserCommonService userCommonService,
                           AuthenticationManager authenticationManager,
                           AmazonS3 s3Client,
                           AmazonSimpleEmailService amazonSimpleEmailService){
        this.userRepository = userRepository;
        this.otpTempRepository = otpTempRepository;
        this.jwtService = jwtService;
        this.profileRepository = profileRepository;
        this.userCommonService = userCommonService;
        this.authenticationManager = authenticationManager;
        this.s3Client = s3Client;
        this.amazonSimpleEmailService = amazonSimpleEmailService;
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

        if(roleId != 1) {
            saveUserProfileDetails(user.getId(), userProfile);
            /** send successful email in a separate thread by aws ses **/
            new Thread(() ->
                    sendEmailToUserForSuccessfulSignupUsingAwsSes(userProfile.getUsername())
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

    private void sendEmailToUserForSuccessfulSignupUsingAwsSes(String email){
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(ADMIN_EMAIL);
        simpleMailMessage.setTo(email);
        simpleMailMessage.setSubject("MoneyFi - Signup Successful");
        simpleMailMessage.setText("Welcome to our app! Enjoy the benefits of MoneyFi with exclusive features");
        sendEmailToUserUsingAwsSes(simpleMailMessage);
    }

    private void sendEmailToUserUsingAwsSes(SimpleMailMessage simpleMailMessage) {
        Destination destination =  new Destination();
        destination.setToAddresses(Arrays.asList(simpleMailMessage.getTo()));

        Content content = new Content();
        content.setData(simpleMailMessage.getText());

        Content subject = new Content();
        subject.setData(simpleMailMessage.getSubject());

        Body body = new Body();
        body.setText(content);

        Message msg = new Message();
        msg.setBody(body);
        msg.setSubject(subject);

        SendEmailRequest emailRequest = new SendEmailRequest();
        emailRequest.setSource(simpleMailMessage.getFrom());
        emailRequest.setDestination(destination);
        emailRequest.setMessage(msg);

        amazonSimpleEmailService.sendEmail(emailRequest);
    }

    @Override
    @Transactional
    public ResponseEntity<?> login(UserAuthModel userAuthModel) {

        makeOldSessionInActiveOfUserForNewLogin(userAuthModel);

        try {
            if (userAuthModel.getUsername() == null || userAuthModel.getUsername().isEmpty() ||
                    userAuthModel.getPassword() == null || userAuthModel.getPassword().isEmpty()) {

                return ResponseEntity.badRequest().body("Username and password are required");
            }

            UserAuthModel existingUser = userRepository.getUserDetailsByUsername(userAuthModel.getUsername());
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
                Authentication authentication = authenticationManager
                        .authenticate(new UsernamePasswordAuthenticationToken(userAuthModel.getUsername(), userAuthModel.getPassword()));

                if (authentication.isAuthenticated()) {
                    JwtToken token = jwtService.generateToken(userAuthModel);
                    functionToPreventMultipleLogins(userAuthModel, token);
                    return ResponseEntity.ok(token);
                }
            } catch (BadCredentialsException ex) {
                ex.printStackTrace();
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect password");
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during login");
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

        UserAuthModel userAuthModel = userRepository.getUserDetailsByUsername(email);
        if(userAuthModel == null){
            remainingTimeCountDto.setComment("User not exist");
            remainingTimeCountDto.setResult(false);
            return remainingTimeCountDto;
        }
        else if(userAuthModel.isBlocked()){
            remainingTimeCountDto.setComment("Account Blocked! Please contact admin");
            remainingTimeCountDto.setResult(false);
            return remainingTimeCountDto;
        }
        else if(userAuthModel.isDeleted()){
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

        if(user != null){
            new Thread(()->
                    otpTempRepository.deleteByEmail(email)
            ).start();
        }
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

        Long userId = userRepository.getUserDetailsByUsername(jwtService.extractUserName(token)).getId();
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
        return EmailTemplates.sendUserNameToUser(username);
    }

    @Override
    public boolean sendAccountStatementEmail(String username, byte[] pdfBytes) {
        return EmailTemplates.sendAccountStatementAsEmail(profileRepository.findByUserId(getUserIdByUsername(username)).getName(), username, pdfBytes);
    }

    @Override
    public String uploadUserProfilePictureToS3(String username, MultipartFile file) throws IOException {
        File fileObj = null;

        try {
            fileObj = convertMultiPartFileToFile(file);
            String fileName = generateFileNameForUserProfilePicture(getUserIdByUsername(username), username);

            s3Client.putObject(new PutObjectRequest(bucketName, fileName, fileObj));
            return "Profile Picture Uploaded!";
        } catch (AmazonServiceException e) {
            e.printStackTrace();
            throw new S3AwsErrorThrowException("Exception occurred while uploading profile picture");
        } finally {
            if (fileObj != null && fileObj.exists()) {
                try {
                    Files.delete(fileObj.toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public ResponseEntity<ByteArrayResource> fetchUserProfilePictureFromS3(String username) {

        try {
            S3Object s3Object = s3Client.getObject(bucketName,
                    generateFileNameForUserProfilePicture(getUserIdByUsername(username), username));
            S3ObjectInputStream inputStream = s3Object.getObjectContent();

            byte[] data = IOUtils.toByteArray(inputStream);
            ByteArrayResource resource = new ByteArrayResource(data);
            return ResponseEntity
                    .ok()
                    .contentLength(data.length)
                    .header("Content-type", "application/octet-stream")
                    .header("Content-disposition", "attachment; filename=\"" + "profile_picture" + ".jpg" + "\"")
                    .body(resource);
        } catch (AmazonServiceException e) {
            e.printStackTrace();
            throw new S3AwsErrorThrowException("Exception occurred while fetching profile picture");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public ResponseEntity<String> deleteProfilePictureFromS3(String username) {

        try {
            s3Client.deleteObject(bucketName,
                    generateFileNameForUserProfilePicture(getUserIdByUsername(username), username));
            return ResponseEntity.ok().body("profile_picture_removed");
        } catch (AmazonServiceException e) {
            e.printStackTrace();
            throw new S3AwsErrorThrowException("Exception occurred while deleting the profile picture");
        }
    }

    private String generateFileNameForUserProfilePicture(Long userId, String username){
        return "profile_pic_" + (userId+143) +
                username.substring(0,username.indexOf('@'));
    }

    private File convertMultiPartFileToFile(MultipartFile file){
        File convertedFile = new File(file.getOriginalFilename());

        try (FileOutputStream fos = new FileOutputStream(convertedFile)){
            fos.write(file.getBytes());
        } catch (IOException e){
            e.printStackTrace();
        }
        return convertedFile;
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




    @Scheduled(fixedRate = 3600000) // Method Runs for every 1 hour
    public void removeOtpCountOfPreviousDay(){
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        List<UserAuthModel>  userAuthModelList = userRepository.getUserListWhoseOtpCountGreaterThanThree(startOfToday);

        for (UserAuthModel userAuthModel : userAuthModelList) {
            userAuthModel.setOtpCount(0);
            userRepository.save(userAuthModel);
        }
    }

}
