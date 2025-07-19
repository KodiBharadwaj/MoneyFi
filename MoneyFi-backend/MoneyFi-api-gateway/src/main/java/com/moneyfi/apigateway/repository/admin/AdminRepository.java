package com.moneyfi.apigateway.repository.admin;

import com.moneyfi.apigateway.model.common.ContactUs;
import com.moneyfi.apigateway.service.admin.dto.response.AdminOverviewPageDto;
import com.moneyfi.apigateway.service.admin.dto.response.UserGridDto;

import java.util.List;

public interface AdminRepository {
    AdminOverviewPageDto getAdminOverviewPageDetails();

    List<ContactUs> getContactUsDetailsOfUsers();

    List<UserGridDto> getUserDetailsGridForAdmin(String status);
}
