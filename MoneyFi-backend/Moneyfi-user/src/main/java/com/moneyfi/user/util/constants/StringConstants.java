package com.moneyfi.user.util.constants;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moneyfi.user.model.general.ProfileModel;
import com.moneyfi.user.repository.general.ProfileRepository;
import jakarta.validation.constraints.NotNull;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

import static com.moneyfi.constants.constants.CommonConstants.userRoleAssociation;

public class StringConstants {

    private StringConstants() {}

    public static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public static final String ALL = "All";
    public static final String LOCAL_PROFILE = "local";
    public static final String PROD_PROFILE = "prod";
    public static final String USER_PROFILE_DETAILS_NOT_FOUND = "User profile details not found";
    public static final String UPLOAD_PROFILE_PICTURE = "profile_pic_";
    public static final String UPLOAD_USER_RAISED_REPORT_PICTURE = "user_defect_pic_";
    public static final String GMAIL_SYNC_COUNT_INCREASE_REQUEST = "gmail_sync_count_request_";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String USER_PROFILE_NOT_FOUND = "User profile not found";
    public static final String REFERENCE_NUMBER_SENT_MESSAGE = "Reference Number sent to your email";
    public static final String INVALID_REQUEST_MESSAGE = "Invalid request details";
    public static final String ACCOUNT_DELETED_MESSAGE = "Account is deleted. Raise retrieval request";
    public static final String INCORRECT_REFERENCE_NUMBER = "Incorrect Reference Number!";
    public static final String REQUEST_NOT_FOUND = "Request details not found";
    public static final String USER_ALREADY_EXISTING_MESSAGE = "User already exists";
    public static final String EMAIL_SENT_SUCCESS_MESSAGE = "Email sent successfully!";
    public static final String INVALID_OTP_MESSAGE = "Invalid or expired OTP";
    public static final String ERROR = "error";
    public static final String USERNAME_PASSWORD_REQUIRED = "Username and password are required";
    public static final String USER_NOT_FOUND_SIGNUP = "User not found. Please sign up";
    public static final String ACCOUNT_BLOCKED = "Account Blocked! Please contact admin";
    public static final String ACCOUNT_DELETED = "Account Deleted! Please contact admin";
    public static final String INVALID_CREDENTIALS = "Invalid Credentials Entered";
    public static final String LOGIN_ERROR = "An error occurred during login";
    public static final String OTP_RESEND_FAILURE_MESSAGE = "Otp not found or expired. Get a new otp";
    public static final long SESSION_LOGIN_MINUTES = 60L;
    public static final String INCORRECT_PASSWORD = "Incorrect password entered";
    public static final String VERIFICATION_CODE_SENT_MESSAGE = "Verification code sent to your email!";
    public static final String MESSAGE = "message";
    public static final String INCORRECT_OLD_PASSWORD = "Incorrect old password";
    public static final String VERIFICATION_SUCCESSFUL_MESSAGE = "Verification successful!";
    public static final String VERIFICATION_FAILURE_MESSAGE = "Invalid or expired verification code";
    public static final String PASSWORD_UPDATED_SUCCESSFULLY = "Password updated successfully!";
    public static final String PASSWORD_UPDATED_MODE_USING_FORGOT = "Password changed using forgot password";
    public static final String EMAIL_LIMIT_CROSSED = "Limit crossed for today!! Try tomorrow";
    public static final String SAME_PASSWORD_NOT_ALLOWED_MESSAGE = "New password cannot be same as old password";
    public static final String LOGOUT_SUCCESS_MESSAGE = "Logged out successfully";
    public static final String LOGOUT_FAILURE_MESSAGE = "Logout failed!";
    public static final String SOMETHING_WENT_WRONG = "Something went wrong";


    public static final ObjectMapper objectMapper = new ObjectMapper();

    public static final String CLIENT_ID = "client_id";
    public static final String CLIENT_SECRET = "client_secret";
    public static final String CODE = "code";
    public static final String GRANT_TYPE = "grant_type";
    public static final String REDIRECT_URI = "redirect_uri";
    public static final String AUTHORIZATION_CODE = "authorization_code";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String EXPIRES_IN = "expires_in";
    public static final String SCOPE = "scope";
    public static final String POST_MESSAGE = "postmessage";
    public static final String STRING_EMAIL = "email";
    public static final String GMAIL_SYNC = "GMAIL_SYNC";
    public static final String GOOGLE_AUTHORIZATION_CODE_NULL_MESSAGE = "Google Authorization code cannot be null";

    public static final String PHONE_NUMBER_DIGITS_ONLY_MESSAGE = "Phone number must contain only digits";
    public static final String PHONE_NUMBER_MAX_LENGTH_MESSAGE = "Phone number should be 10 digits";
    public static final String INVALID_EXCEL_FORMAT = "Invalid excel format";
    public static final String PROFILE_TEMPLATE_EXCEL_NAME = "profile-template.xlsx";
    public static final String EXCEL_TEMPLATE_EXIST_MESSAGE = "Excel template already exists";
    public static final String TEMPLATE_NOT_FOUND = "Excel template not found";
    public static final String GMAIL_AUTH_NOT_FOUND = "Gmail Auth consent not found";

    public static final String CLOUDINARY_CLOUD_NAME = "cloud_name";
    public static final String CLOUDINARY_API_KEY = "api_key";
    public static final String CLOUDINARY_API_SECRET = "api_secret";
    public static final String CLOUDINARY_SECURE = "secure";
    public static final String BLOCKED_BY_ADMIN = "Blocked by Admin";
    public static final String BLOCKED_BY_USER = "Blocked by User";

    public static final Map<String, Integer> templateIdAssociation = Map.of("profile-template", 1);

    public static final String DATE_TIME_PATTERN = "MM/dd/yyyy HH:mm:ss";

    public static int functionToGetRoleIdBasedOnRoleName(@NotNull String role) {
        int roleId = 0;
        for (Map.Entry<Integer, String> it : userRoleAssociation.entrySet()) {
            if (it.getValue().equalsIgnoreCase(role)) {
                roleId = it.getKey();
            }
        }
        return roleId;
    }

    public static String generateFileNameForPictureUpload(Long id, String username, String uploadPurpose) {
        return uploadPurpose + (id) + "_" +
                username.substring(0,username.indexOf('@'));
    }

    public static String functionToGetNameOfUserWithUserId(ProfileRepository profileRepository, Long userId) {
        Optional<ProfileModel> userProfile = profileRepository.findByUserId(userId);
        String name = "";
        if(userProfile.isPresent()){
            name = userProfile.get().getName();
        }
        return name;
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
}
