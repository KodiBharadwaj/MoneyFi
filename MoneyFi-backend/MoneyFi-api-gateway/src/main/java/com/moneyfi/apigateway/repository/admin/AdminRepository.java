package com.moneyfi.apigateway.repository.admin;

import com.moneyfi.apigateway.service.admin.dto.response.AdminOverviewPageDto;
import com.moneyfi.apigateway.service.admin.dto.response.UserGridDto;
import com.moneyfi.apigateway.service.admin.dto.response.UserProfileAndRequestDetailsDto;
import com.moneyfi.apigateway.service.admin.dto.response.UserRequestsGridDto;

import java.util.List;
import java.util.Map;

public interface AdminRepository {
    AdminOverviewPageDto getAdminOverviewPageDetails();

    List<UserRequestsGridDto> getUserRequestsGridForAdmin(String requestReason);

    List<UserGridDto> getUserDetailsGridForAdmin(String status);

    Map<Integer, Integer> getUserMonthlyCountInAYear(int year, String status);

    UserProfileAndRequestDetailsDto getCompleteUserDetailsForAdmin(String username);
}
