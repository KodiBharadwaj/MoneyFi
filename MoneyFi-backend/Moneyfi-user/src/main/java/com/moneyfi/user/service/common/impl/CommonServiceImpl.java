package com.moneyfi.user.service.common.impl;

import com.moneyfi.user.repository.ProfileRepository;
import com.moneyfi.user.service.common.CommonService;
import com.moneyfi.user.util.EmailTemplates;
import org.springframework.stereotype.Service;

import static com.moneyfi.user.util.constants.StringUtils.functionToGetNameOfUserWithUserId;

@Service
public class CommonServiceImpl implements CommonService {

    private final ProfileRepository profileRepository;
    private final EmailTemplates emailTemplates;

    public CommonServiceImpl(ProfileRepository profileRepository,
                             EmailTemplates emailTemplates) {
        this.profileRepository = profileRepository;
        this.emailTemplates = emailTemplates;
    }

    @Override
    public boolean sendAccountStatementEmail(String username, Long userId, byte[] pdfBytes) {
        String name = functionToGetNameOfUserWithUserId(profileRepository, userId);
        try {
            return emailTemplates.sendAccountStatementAsEmail(!name.trim().isEmpty() ? name : "User", username, pdfBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean sendSpendingAnalysisEmail(String username, Long userId, byte[] pdfBytes) {
        String name = functionToGetNameOfUserWithUserId(profileRepository, userId);
        try {
            return emailTemplates.sendSpendingAnalysisEmail(!name.trim().isEmpty() ? name : "User", username, pdfBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
