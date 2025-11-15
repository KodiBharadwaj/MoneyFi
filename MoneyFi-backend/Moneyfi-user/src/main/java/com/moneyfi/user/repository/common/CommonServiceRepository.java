package com.moneyfi.user.repository.common;

import com.moneyfi.user.service.common.dto.response.UserNotificationResponseDto;
import com.moneyfi.user.service.common.dto.response.UserRequestStatusDto;
import com.moneyfi.user.service.profile.dto.ProfileDetailsDto;

import java.util.List;

public interface CommonServiceRepository {
    ProfileDetailsDto getProfileDetailsOfUser(String username);

    UserRequestStatusDto trackUserRequestUsingReferenceNumber(String referenceNumber);

    List<UserNotificationResponseDto> getUserNotifications(String username);

    List<String> getBirthdayAndAnniversaryUsersList(int month, int day, String occasion);
}
