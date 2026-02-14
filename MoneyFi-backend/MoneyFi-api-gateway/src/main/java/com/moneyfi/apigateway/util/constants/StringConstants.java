package com.moneyfi.apigateway.util.constants;

import com.moneyfi.apigateway.dto.ContactUs;
import com.moneyfi.apigateway.dto.ContactUsHist;
import com.moneyfi.apigateway.dto.ProfileModel;
import com.moneyfi.apigateway.dto.interfaces.ContactUsHistProjection;
import com.moneyfi.apigateway.dto.interfaces.ContactUsProjection;
import com.moneyfi.apigateway.dto.interfaces.ProfileDetailsProjection;
import com.moneyfi.apigateway.util.enums.ReasonEnum;
import com.moneyfi.apigateway.util.enums.UserRoles;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.net.URL;
import java.net.HttpURLConnection;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;

import static com.moneyfi.apigateway.util.enums.ReasonEnum.*;

public class StringConstants {

    private StringConstants() {}

    public static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public static final String MESSAGE = "message";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String EMAIL_SENT_SUCCESS_MESSAGE = "Email sent successfully!";
    public static final String VERIFICATION_CODE_SENT_MESSAGE = "Verification code sent to your email!";
    public static final String INCORRECT_PASSWORD = "Incorrect password entered";
    public static final String INCORRECT_OLD_PASSWORD = "Incorrect old password";
    public static final String USER_ALREADY_EXISTING_MESSAGE = "User already exists";
    public static final String VERIFICATION_SUCCESSFUL_MESSAGE = "Verification successful!";
    public static final String VERIFICATION_FAILURE_MESSAGE = "Invalid or expired verification code";
    public static final String PASSWORD_UPDATED_SUCCESSFULLY = "Password updated successfully!";
    public static final String PASSWORD_UPDATED_MODE_USING_FORGOT = "Password changed using forgot password";
    public static final String EMAIL_LIMIT_CROSSED = "Limit crossed for today!! Try tomorrow";
    public static final String SAME_PASSWORD_NOT_ALLOWED_MESSAGE = "New password cannot be same as old password";
    public static final String INVALID_OTP_MESSAGE = "Invalid or expired OTP";
    public static final String PHONE_NUMBER_DIGITS_ONLY_MESSAGE = "Phone number must contain only digits";
    public static final String PHONE_NUMBER_MAX_LENGTH_MESSAGE = "Phone number should be 10 digits";
    public static final String LOGOUT_SUCCESS_MESSAGE = "Logged out successfully";
    public static final String LOGOUT_FAILURE_MESSAGE = "Logout failed!";
    public static final String ERROR = "error";
    public static final String USERNAME_PASSWORD_REQUIRED = "Username and password are required";
    public static final String USER_NOT_FOUND_SIGNUP = "User not found. Please sign up";
    public static final String ACCOUNT_BLOCKED = "Account Blocked! Please contact admin";
    public static final String ACCOUNT_DELETED = "Account Deleted! Please contact admin";
    public static final String INVALID_CREDENTIALS = "Invalid Credentials Entered";
    public static final String LOGIN_ERROR = "An error occurred during login";

    public static final long SESSION_LOGIN_MINUTES = 60L;

    public static final Map<Integer, String> userRoleAssociation = Map.of(1, UserRoles.ADMIN.name(), 2, UserRoles.USER.name(), 3, UserRoles.DEVELOPER.name(), 4, UserRoles.MAINTAINER.name());
    public static final Map<ReasonEnum, Integer> reasonCodeIdAssociation =
            Map.ofEntries(
                    Map.entry(BLOCK_ACCOUNT, 1),
                    Map.entry(PASSWORD_CHANGE, 2),
                    Map.entry(NAME_CHANGE, 3),
                    Map.entry(UNBLOCK_ACCOUNT, 4),
                    Map.entry(DELETE_ACCOUNT, 5),
                    Map.entry(ACCOUNT_RETRIEVAL, 6),
                    Map.entry(PHONE_NUMBER_CHANGE, 7),
                    Map.entry(FORGOT_PASSWORD, 8),
                    Map.entry(USER_RAISED_REQUEST_IGNORED, 9),
                    Map.entry(FORGOT_USERNAME, 10),
                    Map.entry(ADMIN_CREATION, 11),
                    Map.entry(ADMIN_UPDATE, 12),
                    Map.entry(ADMIN_BLOCK, 13),
                    Map.entry(ADMIN_UNBLOCK, 14),
                    Map.entry(ADMIN_DELETE, 15),
                    Map.entry(ADMIN_RETRIEVAL, 16)
            );
    public static final String DATE_TIME_PATTERN = "MM/dd/yyyy HH:mm:ss";
    public static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public static final String USER_SERVICE_URL_FOR_ADMIN = "http://MONEYFI-USER/api/v1/user-service/admin";
    public static final String USER_SERVICE_OPEN_URL = "http://MONEYFI-USER/api/v1/user-service/open";

    public static String generateVerificationCode() {
        Random random = new Random();
        int verificationCode = 100000 + random.nextInt(900000);
        return String.valueOf(verificationCode);
    }

    public static MultipartFile convertImageUrlToMultipartFile(String imageUrl) throws Exception {
        URL url = new URL(imageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(false);
        connection.connect();

        try (InputStream inputStream = connection.getInputStream()) {
            MultipartFile multipartFile = new MockMultipartFile(
                    "file",
                    imageUrl,
                    "image/jpeg",
                    inputStream
            );
            return multipartFile;
        }
    }

    public static ContactUs convertContactUsInterfaceToDto(ContactUsProjection contactUsProjection) {
        ContactUs contactUs = new ContactUs();
        contactUs.setId(contactUsProjection.getId());
        contactUs.setEmail(contactUsProjection.getEmail());
        contactUs.setReferenceNumber(contactUsProjection.getReferenceNumber());
        contactUs.setRequestActive(contactUsProjection.getIsRequestActive());
        contactUs.setRequestReason(contactUsProjection.getRequestReason());
        contactUs.setVerified(contactUsProjection.getIsVerified());
        contactUs.setRequestStatus(contactUsProjection.getRequestStatus());
        contactUs.setStartTime(contactUsProjection.getStartTime());
        contactUs.setCompletedTime(contactUsProjection.getCompletedTime());
        return contactUs;
    }

    public static ProfileModel convertProfileDetailsInterfaceToDto(ProfileDetailsProjection profileDetailsProjection) {
        ProfileModel profileModel = new ProfileModel();
        profileModel.setId(profileDetailsProjection.getId());
        profileModel.setUserId(profileDetailsProjection.getUserId());
        profileModel.setName(profileDetailsProjection.getName());
        profileModel.setCreatedDate(profileDetailsProjection.getCreatedDate());
        profileModel.setPhone(profileDetailsProjection.getPhone());
        profileModel.setGender(profileDetailsProjection.getGender());
        profileModel.setDateOfBirth(profileDetailsProjection.getDateOfBirth());
        profileModel.setMaritalStatus(profileDetailsProjection.getMaritalStatus());
        profileModel.setAddress(profileDetailsProjection.getAddress());
        profileModel.setIncomeRange(profileDetailsProjection.getIncomeRange());
        return profileModel;
    }

    public static ContactUsHist convertContactUsHistInterfaceToDto(ContactUsHistProjection contactUsHistProjection) {
        ContactUsHist contactUsHist = new ContactUsHist();
        contactUsHist.setId(contactUsHistProjection.getId());
        contactUsHist.setContactUsId(contactUsHistProjection.getContactUsId());
        contactUsHist.setName(contactUsHistProjection.getName());
        contactUsHist.setMessage(contactUsHistProjection.getMessage());
        contactUsHist.setUpdatedTime(contactUsHistProjection.getUpdatedTime());
        contactUsHist.setRequestReason(contactUsHistProjection.getRequestReason());
        contactUsHist.setRequestStatus(contactUsHistProjection.getRequestStatus());
        return contactUsHist;
    }
}
