package com.moneyfi.apigateway.service.admin.impl;

import com.moneyfi.apigateway.exceptions.ScenarioNotPossibleException;
import com.moneyfi.apigateway.model.auth.UserAuthModel;
import com.moneyfi.apigateway.model.common.ContactUs;
import com.moneyfi.apigateway.model.common.ProfileModel;
import com.moneyfi.apigateway.repository.admin.AdminRepository;
import com.moneyfi.apigateway.repository.user.ContactUsRepository;
import com.moneyfi.apigateway.repository.user.ProfileRepository;
import com.moneyfi.apigateway.repository.user.auth.UserRepository;
import com.moneyfi.apigateway.service.admin.AdminService;
import com.moneyfi.apigateway.service.admin.dto.AdminOverviewPageDto;
import com.moneyfi.apigateway.service.admin.dto.UserGridDto;
import com.moneyfi.apigateway.util.constants.RequestReason;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final ContactUsRepository contactUsRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    public AdminServiceImpl(AdminRepository adminRepository,
                            ContactUsRepository contactUsRepository,
                            UserRepository userRepository,
                            ProfileRepository profileRepository){
        this.adminRepository = adminRepository;
        this.contactUsRepository = contactUsRepository;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
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
    public boolean accountReactivationAndNameChangeRequest(String email, String referenceNumber, String requestStatus) {
        return contactUsRepository.findByEmail(email)
                .stream()
                .filter(ContactUs::isRequestActive)
                .filter(i -> i.getReferenceNumber().equals(referenceNumber))
                .findFirst()
                .map(i -> {
                    functionCallToChangeDetails(email, i, requestStatus);
                    return true;
                })
                .orElse(false);
    }

    private void functionCallToChangeDetails(String email, ContactUs contactUs, String requestStatus){
        UserAuthModel user = userRepository.getUserAuthDetailsByOnlyUsername(email);

        if(requestStatus.equalsIgnoreCase(RequestReason.ACCOUNT_UNBLOCK_REQUEST.name())){
            user.setBlocked(false);
            userRepository.save(user);

            contactUs.setRequestActive(false);
            contactUs.setVerified(true);
            contactUsRepository.save(contactUs);
        }
        else if (requestStatus.equalsIgnoreCase(RequestReason.NAME_CHANGE_REQUEST.name())){
            ProfileModel userProfile = profileRepository.findByUserId(user.getId());
            if(!userProfile.getName().toLowerCase().contains(contactUs.getMessage().toLowerCase())){
                throw new ScenarioNotPossibleException("Old name didn't match");
            }

            userProfile.setName(contactUs.getName());
            profileRepository.save(userProfile);

            contactUs.setRequestActive(false);
            contactUs.setVerified(true);
            contactUsRepository.save(contactUs);
        }
    }

    @Override
    public List<ContactUs> getContactUsDetailsOfUsers() {
        return adminRepository.getContactUsDetailsOfUsers();
    }
}
