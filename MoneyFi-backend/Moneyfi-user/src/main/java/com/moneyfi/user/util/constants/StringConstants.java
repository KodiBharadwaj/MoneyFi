package com.moneyfi.user.util.constants;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moneyfi.user.model.ProfileModel;
import com.moneyfi.user.model.dto.OtpTempModel;
import com.moneyfi.user.model.dto.UserAuthModel;
import com.moneyfi.user.model.dto.interfaces.OtpTempProjection;
import com.moneyfi.user.model.dto.interfaces.UserAuthProjection;
import com.moneyfi.user.repository.ProfileRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Map;
import java.util.Optional;

public class StringConstants {

    private StringConstants() {}

    public static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public static final String ALL = "All";
    public static final String LOCAL_PROFILE = "local";
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

    public static final String PHONE_NUMBER_DIGITS_ONLY_MESSAGE = "Phone number must contain only digits";
    public static final String PHONE_NUMBER_MAX_LENGTH_MESSAGE = "Phone number should be 10 digits";
    public static final String INVALID_EXCEL_FORMAT = "Invalid excel format";
    public static final String PROFILE_TEMPLATE_EXCEL_NAME = "profile-template.xlsx";
    public static final String EXCEL_TEMPLATE_EXIST_MESSAGE = "Excel template already exists";
    public static final String TEMPLATE_NOT_FOUND = "Excel template not found";

    public static final String CLOUDINARY_CLOUD_NAME = "cloud_name";
    public static final String CLOUDINARY_API_KEY = "api_key";
    public static final String CLOUDINARY_API_SECRET = "api_secret";
    public static final String CLOUDINARY_SECURE = "secure";
    public static final String BLOCKED_BY_ADMIN = "Blocked by Admin";
    public static final String BLOCKED_BY_USER = "Blocked by User";

    public static final Map<String, Integer> templateIdAssociation = Map.of("profile-template", 1);

    public static final String DATE_TIME_PATTERN = "MM/dd/yyyy HH:mm:ss";

    public static final ObjectMapper objectMapper = new ObjectMapper();

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

    public static UserAuthModel convertUserAuthInterfaceToDto(UserAuthProjection userAuthProjection) {
        UserAuthModel userAuthModel = new UserAuthModel();
        userAuthModel.setId(userAuthProjection.getId());
        userAuthModel.setUsername(userAuthProjection.getUsername());
        userAuthModel.setPassword(userAuthProjection.getPassword());
        userAuthModel.setVerificationCode(userAuthProjection.getVerificationCode());
        userAuthModel.setVerificationCodeExpiration(userAuthProjection.getVerificationCodeExpiration());
        userAuthModel.setOtpCount(userAuthProjection.getOtpCount());
        userAuthModel.setBlocked(userAuthProjection.getIsBlocked());
        userAuthModel.setDeleted(userAuthProjection.getIsDeleted());
        userAuthModel.setLoginCodeValue(userAuthProjection.getLoginCodeValue());
        userAuthModel.setRoleId(userAuthProjection.getRoleId());
        return userAuthModel;
    }

    public static OtpTempModel convertOtpTempModelInterfaceToDto(OtpTempProjection otpTempProjection) {
        OtpTempModel otpTempModel = new OtpTempModel();
        otpTempModel.setEmail(otpTempProjection.getEmail());
        otpTempModel.setId(otpTempProjection.getId());
        otpTempModel.setOtp(otpTempProjection.getOtp());
        otpTempModel.setOtpType(otpTempProjection.getOtpType());
        otpTempModel.setExpirationTime(otpTempProjection.getExpirationTime());
        return otpTempModel;
    }
}
