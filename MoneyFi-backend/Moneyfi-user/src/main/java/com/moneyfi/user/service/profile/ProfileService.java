package com.moneyfi.user.service.profile;

import com.moneyfi.user.model.ProfileModel;
import com.moneyfi.user.service.common.dto.request.UserDefectRequestDto;
import com.moneyfi.user.service.common.dto.request.UserFeedbackRequestDto;
import com.moneyfi.user.service.profile.dto.ProfileDetailsDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {

    ProfileDetailsDto saveUserDetails(Long userId, ProfileModel profile);

    ProfileDetailsDto getProfileDetailsOfUser(String username);

    String getUserDetailsByUserId(Long userId);

    void saveContactUsDetails(UserDefectRequestDto userDefectRequestDto, Long userId, String username);

    void saveFeedback(UserFeedbackRequestDto feedback, Long userId);

    void parseUserProfileDataFromExcel(MultipartFile excel, Long userId);

    ResponseEntity<byte[]> downloadTemplateForUserProfile();
}
