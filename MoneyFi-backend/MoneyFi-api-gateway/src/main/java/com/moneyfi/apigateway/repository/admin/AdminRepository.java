package com.moneyfi.apigateway.repository.admin;

import com.moneyfi.apigateway.model.common.ContactUs;
import com.moneyfi.apigateway.service.admin.dto.AdminOverviewPageDto;

import java.util.List;

public interface AdminRepository {
    AdminOverviewPageDto getAdminOverviewPageDetails();

    List<ContactUs> getContactUsDetailsOfUsers();
}
