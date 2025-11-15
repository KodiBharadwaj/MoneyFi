package com.moneyfi.user.repository.admin;

import com.moneyfi.user.service.admin.dto.response.*;
import com.moneyfi.user.service.common.dto.response.UserFeedbackResponseDto;

import java.util.List;
import java.util.Map;

public interface AdminRepository {
    AdminOverviewPageDto getAdminOverviewPageDetails();

    List<UserRequestsGridDto> getUserRequestsGridForAdmin(String requestReason);

    List<UserGridDto> getUserDetailsGridForAdmin(String status);

    Map<Integer, Integer> getUserMonthlyCountInAYear(int year, String status);

    UserProfileAndRequestDetailsDto getCompleteUserDetailsForAdmin(String username);

    List<UserDefectResponseDto> getUserRaisedDefectsForAdmin();

    List<UserFeedbackResponseDto> getUserFeedbackListForAdmin();

    List<AdminSchedulesResponseDto> getAllActiveSchedulesOfAdmin();
}
