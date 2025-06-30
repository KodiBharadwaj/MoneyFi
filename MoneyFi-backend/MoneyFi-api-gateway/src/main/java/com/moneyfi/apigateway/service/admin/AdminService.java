package com.moneyfi.apigateway.service.admin;

import com.moneyfi.apigateway.model.common.ContactUs;
import com.moneyfi.apigateway.service.admin.dto.AdminOverviewPageDto;

import java.util.List;

public interface AdminService {
    AdminOverviewPageDto getAdminOverviewPageDetails();

    List<ContactUs> getContactUsDetailsOfUsers();
}
