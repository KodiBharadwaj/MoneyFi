package com.moneyfi.apigateway.service.admin;

import com.moneyfi.apigateway.service.admin.dto.response.*;

import java.util.List;
import java.util.Map;

public interface AdminService {
    AdminOverviewPageDto getAdminOverviewPageDetails();

    List<UserRequestsGridDto> getUserRequestsGridForAdmin(String status);

    List<UserDefectResponseDto> getUserRaisedDefectsForAdmin(String status);

    List<UserGridDto> getUserDetailsGridForAdmin(String status);

    byte[] getUserDetailsExcelForAdmin(String status);

    boolean accountReactivationAndNameChangeRequest(String email, String referenceNumber, String requestStatus);

    Map<Integer, Integer> getUserMonthlyCountInAYear(int year, String status);

    UserProfileAndRequestDetailsDto getCompleteUserDetailsForAdmin(String username);
}
