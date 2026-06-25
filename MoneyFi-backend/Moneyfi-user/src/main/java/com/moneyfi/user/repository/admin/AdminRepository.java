package com.moneyfi.user.repository.admin;

import com.moneyfi.constants.dto.PaginatedRequestDto;
import com.moneyfi.user.service.admin.dto.response.*;
import com.moneyfi.user.service.user.dto.response.UserFeedbackResponseDto;

import java.util.List;
import java.util.Map;

public interface AdminRepository {
    AdminOverviewPageDto getAdminOverviewPageDetails();

    List<UserRequestsGridDto> getUserRequestsGridForAdmin(String requestReason, PaginatedRequestDto requestDto);

    List<UserGridDto> getUserDetailsGridForAdmin(String status, Long offset, Long limit, String search, String searchBy);

    Map<Integer, Integer> getUserMonthlyCountInAYear(int year, String status);

    UserProfileAndRequestDetailsDto getCompleteUserDetailsForAdmin(String username);

    List<UserDefectResponseDto> getUserRaisedDefectsForAdmin(PaginatedRequestDto requestDto);

    List<UserFeedbackResponseDto> getUserFeedbackListForAdmin(PaginatedRequestDto requestDto);

    List<AdminSchedulesResponseDto> getAllActiveSchedulesOfAdmin(String status, String operationMode);
}
