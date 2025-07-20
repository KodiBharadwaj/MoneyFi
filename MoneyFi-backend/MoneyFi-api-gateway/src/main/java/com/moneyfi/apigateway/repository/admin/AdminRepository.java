package com.moneyfi.apigateway.repository.admin;

import com.moneyfi.apigateway.model.common.ContactUs;
import com.moneyfi.apigateway.service.admin.dto.response.AdminOverviewPageDto;
import com.moneyfi.apigateway.service.admin.dto.response.UserGridDto;
import com.moneyfi.apigateway.service.admin.dto.response.UserRequestsGridDto;

import java.util.List;

public interface AdminRepository {
    AdminOverviewPageDto getAdminOverviewPageDetails();

    List<UserRequestsGridDto> getUserRequestsGridForAdmin(String requestReason);

    List<UserGridDto> getUserDetailsGridForAdmin(String status);
}
