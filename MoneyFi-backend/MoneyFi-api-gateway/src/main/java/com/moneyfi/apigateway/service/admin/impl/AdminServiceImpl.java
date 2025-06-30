package com.moneyfi.apigateway.service.admin.impl;

import com.moneyfi.apigateway.model.common.ContactUs;
import com.moneyfi.apigateway.repository.admin.AdminRepository;
import com.moneyfi.apigateway.service.admin.AdminService;
import com.moneyfi.apigateway.service.admin.dto.AdminOverviewPageDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;

    public AdminServiceImpl(AdminRepository adminRepository){
        this.adminRepository = adminRepository;
    }

    @Override
    public AdminOverviewPageDto getAdminOverviewPageDetails() {
        return adminRepository.getAdminOverviewPageDetails();
    }

    @Override
    public List<ContactUs> getContactUsDetailsOfUsers() {
        return adminRepository.getContactUsDetailsOfUsers();
    }
}
