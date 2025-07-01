package com.moneyfi.apigateway.service.admin;

import com.moneyfi.apigateway.model.common.ContactUs;
import com.moneyfi.apigateway.service.admin.dto.AdminOverviewPageDto;
import com.moneyfi.apigateway.service.admin.dto.UserGridDto;

import java.util.List;

public interface AdminService {
    AdminOverviewPageDto getAdminOverviewPageDetails();

    List<ContactUs> getContactUsDetailsOfUsers();

    List<UserGridDto> getUserDetailsGridForAdmin(String status);

    boolean accountReactivationRequest(String email, String referenceNumber);
}
