package com.moneyfi.user.service.primary.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.moneyfi.user.model.primary.general.ProfileModel;
import com.moneyfi.user.service.primary.user.dto.request.UserDefectRequestDto;
import com.moneyfi.user.service.primary.user.dto.request.UserFeedbackRequestDto;
import com.moneyfi.user.service.primary.user.dto.response.ProfileDetailsDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProfileService {

    ProfileDetailsDto saveUserDetails(String username, Long userId, ProfileModel profile);

    ProfileDetailsDto getProfileDetailsOfUser(String username);

    String getUserDetailsByUserId(Long userId);

    void saveContactUsDetails(UserDefectRequestDto userDefectRequestDto, Long userId, String username) throws IOException;

    void saveFeedback(UserFeedbackRequestDto feedback, Long userId) throws JsonProcessingException;

    void parseUserProfileDataFromExcel(MultipartFile excel, Long userId);

    ResponseEntity<byte[]> downloadTemplateForUserProfile(String fileType);
}
