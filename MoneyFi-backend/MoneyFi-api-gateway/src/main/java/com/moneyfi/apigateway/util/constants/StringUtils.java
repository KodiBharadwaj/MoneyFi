package com.moneyfi.apigateway.util.constants;

import com.moneyfi.apigateway.model.common.ProfileModel;
import com.moneyfi.apigateway.util.enums.ReasonEnum;
import com.moneyfi.apigateway.util.enums.UserRoles;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.Random;

import static com.moneyfi.apigateway.util.enums.ReasonEnum.*;

public class StringUtils {

    private StringUtils() {}

    public static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public static final String MESSAGE = "message";
    public static final String USER_ROLE = "USER";
    public static final String ADMIN_ROLE = "ADMIN";
    public static final String LOCAL_PROFILE = "local";
    public static final String USER_PROFILE_DETAILS_NOT_FOUND = "User profile details not found";
    public static final String UPLOAD_PROFILE_PICTURE = "profile_pic_";
    public static final String UPLOAD_USER_RAISED_REPORT_PICTURE = "user_defect_pic_";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String USER_PROFILE_NOT_FOUND = "User profile not found";
    public static final String EMAIL_SENT_SUCCESS_MESSAGE = "Email sent successfully!";
    public static final String INCORRECT_PASSWORD = "Incorrect password entered";
    public static final String INCORRECT_OLD_PASSWORD = "Incorrect old password";

    public static final String PROFILE_TEMPLATE_NAME = "profile-template";
    public static final String PHONE_NUMBER_EMPTY_MESSAGE = "Phone number is empty";
    public static final String PHONE_NUMBER_DIGITS_ONLY_MESSAGE = "Phone number must contain only digits";
    public static final String PHONE_NUMBER_MAX_LENGTH_MESSAGE = "Phone number should be 10 digits";
    public static final String INVALID_EXCEL_FORMAT = "Invalid excel format";
    public static final String LOGOUT_SUCCESS_MESSAGE = "Logged out successfully";
    public static final String LOGOUT_FAILURE_MESSAGE = "Logout failed!";

    public static final String ERROR = "error";
    public static final String USERNAME_PASSWORD_REQUIRED = "Username and password are required";
    public static final String USER_NOT_FOUND_SIGNUP = "User not found. Please sign up";
    public static final String ACCOUNT_BLOCKED = "Account Blocked! Please contact admin";
    public static final String ACCOUNT_DELETED = "Account Deleted! Please contact admin";
    public static final String INVALID_CREDENTIALS = "Invalid Credentials Entered";
    public static final String LOGIN_ERROR = "An error occurred during login";

    public static final String CLOUDINARY_CLOUD_NAME = "cloud_name";
    public static final String CLOUDINARY_API_KEY = "api_key";
    public static final String CLOUDINARY_API_SECRET = "api_secret";
    public static final String CLOUDINARY_SECURE = "secure";
    public static final String BLOCKED_BY_ADMIN = "Blocked by Admin";
    public static final String BLOCKED_BY_USER = "Blocked by User";

    public static final Map<Integer, String> userRoleAssociation = Map.of(1, UserRoles.ADMIN.name(), 2, UserRoles.USER.name(), 3, UserRoles.DEVELOPER.name());
    public static final Map<String, Integer> templateIdAssociation = Map.of("profile-template", 1);
    public static final Map<ReasonEnum, Integer> reasonCodeIdAssociation = Map.of(BLOCK_ACCOUNT, 1, PASSWORD_CHANGE, 2, NAME_CHANGE, 3,
            UNBLOCK_ACCOUNT, 4, DELETE_ACCOUNT, 5, ACCOUNT_RETRIEVAL, 6, PHONE_NUMBER_CHANGE, 7, FORGOT_PASSWORD, 8);

    public static final String DATE_TIME_PATTERN = "MM/dd/yyyy HH:mm:ss";
    public static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public static final String DAILY_QUOTE_EXTERNAL_API_URL = "https://zenquotes.io/api/random";

    public static String generateVerificationCode() {
        Random random = new Random();
        int verificationCode = 100000 + random.nextInt(900000);
        return String.valueOf(verificationCode);
    }

    public static String generateAlphabetCode() {
        StringBuilder code = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 5; i++) {
            int index = random.nextInt(ALPHABET.length());
            code.append(ALPHABET.charAt(index));
        }

        return code.toString();
    }

    public static String generateFileNameForPictureUpload(Long id, String username, String uploadPurpose) {
        return uploadPurpose + (id) + "_" +
                username.substring(0,username.indexOf('@'));
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

    public static String generateReferenceNumberForUserToSendEmail(String referencePrefix, ProfileModel userProfile, String username) {
        return referencePrefix + userProfile.getName().substring(0, 2) + username.substring(0, 2)
                + (userProfile.getPhone() != null ? userProfile.getPhone().substring(0, 2) + generateVerificationCode().substring(0, 3) : generateVerificationCode());
    }
}
