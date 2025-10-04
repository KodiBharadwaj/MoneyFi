package com.moneyfi.apigateway.repository.common;

import com.moneyfi.apigateway.service.common.dto.response.ProfileDetailsDto;
import com.moneyfi.apigateway.service.common.dto.response.UserNotificationResponseDto;
import com.moneyfi.apigateway.service.common.dto.response.UserRequestStatusDto;

import java.util.List;

public interface CommonServiceRepository {
    ProfileDetailsDto getProfileDetailsOfUser(Long userId);

    UserRequestStatusDto trackUserRequestUsingReferenceNumber(String referenceNumber);

    List<UserNotificationResponseDto> getUserNotifications(String username);

    List<String> getBirthdayAndAnniversaryUsersList(int month, int day, String occasion);
}
