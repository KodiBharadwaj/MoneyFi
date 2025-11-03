package com.moneyfi.apigateway.service.common;

import com.moneyfi.apigateway.model.common.ContactUs;
import com.moneyfi.apigateway.model.common.ProfileModel;
import com.moneyfi.apigateway.service.common.dto.request.UserDefectRequestDto;
import com.moneyfi.apigateway.service.common.dto.request.UserFeedbackRequestDto;
import com.moneyfi.apigateway.service.common.dto.response.ProfileDetailsDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {

    ProfileDetailsDto saveUserDetails(Long userId, ProfileModel profile);

    ProfileDetailsDto getProfileDetailsOfUser(String username);

    String getUserDetailsByUserId(Long userId);

    void saveContactUsDetails(UserDefectRequestDto userDefectRequestDto, Long userId, String username);

    void saveFeedback(UserFeedbackRequestDto feedback);

    ResponseEntity<String> parseUserProfileDataFromExcel(MultipartFile excel, Long userId);

    ResponseEntity<byte[]> downloadTemplateForUserProfile();
}
