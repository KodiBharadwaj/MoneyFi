package com.moneyfi.apigateway.service.admin.impl;

import com.moneyfi.apigateway.model.auth.UserAuthModel;
import com.moneyfi.apigateway.model.common.ContactUs;
import com.moneyfi.apigateway.repository.admin.AdminRepository;
import com.moneyfi.apigateway.repository.user.ContactUsRepository;
import com.moneyfi.apigateway.repository.user.auth.UserRepository;
import com.moneyfi.apigateway.service.admin.AdminService;
import com.moneyfi.apigateway.service.admin.dto.AdminOverviewPageDto;
import com.moneyfi.apigateway.service.admin.dto.UserGridDto;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final ContactUsRepository contactUsRepository;
    private final UserRepository userRepository;

    public AdminServiceImpl(AdminRepository adminRepository,
                            ContactUsRepository contactUsRepository,
                            UserRepository userRepository){
        this.adminRepository = adminRepository;
        this.contactUsRepository = contactUsRepository;
        this.userRepository = userRepository;
    }

    @Override
    public AdminOverviewPageDto getAdminOverviewPageDetails() {
        AdminOverviewPageDto overviewPageDetails = adminRepository.getAdminOverviewPageDetails();
        overviewPageDetails.setTotalUsers(overviewPageDetails.getActiveUsers() + overviewPageDetails.getBlockedUsers() + overviewPageDetails.getDeletedUsers());
        return overviewPageDetails;
    }

    @Override
    public List<UserGridDto> getUserDetailsGridForAdmin(String status) {
        List<UserGridDto> userGridDtoList = adminRepository.getUserDetailsGridForAdmin(status);

        AtomicInteger i = new AtomicInteger(1);
        userGridDtoList.forEach(user -> user.setSlNo(i.getAndIncrement()));
        return userGridDtoList;
    }

    @Override
    @Transactional
    public boolean accountReactivationRequest(String email, String referenceNumber) {
        return contactUsRepository.findByEmail(email)
                .stream()
                .filter(ContactUs::isRequestActive)
                .filter(i -> i.getReferenceNumber().equals(referenceNumber))
                .findFirst()
                .map(i -> {
                    functionCallToReactivateAccount(email, i);
                    return true;
                })
                .orElse(false);
    }

    private void functionCallToReactivateAccount(String email, ContactUs contactUs){
        UserAuthModel blockedUser = userRepository.getBlockedUsers(email);
        blockedUser.setBlocked(false);
        userRepository.save(blockedUser);

        contactUs.setRequestActive(false);
        contactUsRepository.save(contactUs);
    }

    @Override
    public List<ContactUs> getContactUsDetailsOfUsers() {
        return adminRepository.getContactUsDetailsOfUsers();
    }
}
