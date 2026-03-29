package com.moneyfi.user.repository.common;

import com.moneyfi.user.service.general.scheduler.dto.UserEventDto;
import com.moneyfi.user.service.user.dto.response.UserNotificationResponseDto;
import com.moneyfi.user.service.user.dto.response.UserRequestStatusDto;
import com.moneyfi.user.service.user.dto.response.ProfileDetailsDto;

import java.util.List;

public interface CommonServiceRepository {
    ProfileDetailsDto getProfileDetailsOfUser(String username);

    UserRequestStatusDto trackUserRequestUsingReferenceNumber(String referenceNumber);

    List<UserNotificationResponseDto> getUserNotifications(String username, String status);

    List<String> findAllUsernamesOfUsers();

    List<String> getCategoriesBasedOnTransactionType(String name);

    List<UserEventDto> getBirthdayAndAnniversaryUsersList(int month, int day, String occasion);
}
