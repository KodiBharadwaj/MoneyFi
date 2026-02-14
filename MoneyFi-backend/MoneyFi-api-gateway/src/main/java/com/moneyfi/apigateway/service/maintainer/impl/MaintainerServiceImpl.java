package com.moneyfi.apigateway.service.maintainer.impl;

import com.moneyfi.apigateway.exceptions.ResourceNotFoundException;
import com.moneyfi.apigateway.model.auth.UserAuthModel;
import com.moneyfi.apigateway.model.common.UserAuthHist;
import com.moneyfi.apigateway.repository.user.UserAuthHistRepository;
import com.moneyfi.apigateway.repository.user.auth.SessionTokenRepository;
import com.moneyfi.apigateway.repository.user.auth.UserRepository;
import com.moneyfi.apigateway.service.maintainer.MaintainerService;
import com.moneyfi.apigateway.service.maintainer.dto.request.CreateOrUpdateAdminRequestDto;
import com.moneyfi.apigateway.service.maintainer.dto.response.AdminUsersResponseDto;
import com.moneyfi.apigateway.util.constants.StringConstants;
import com.moneyfi.apigateway.util.enums.LoginMode;
import com.moneyfi.apigateway.util.enums.UserRoles;
import com.moneyfi.apigateway.validator.UserValidations;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.moneyfi.apigateway.util.constants.StringConstants.*;
import static com.moneyfi.apigateway.util.enums.ReasonEnum.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class MaintainerServiceImpl implements MaintainerService {

    private final UserRepository userRepository;
    private final UserAuthHistRepository userAuthHistRepository;
    private final SessionTokenRepository sessionTokenRepository;

    private static final String BLOCK = "BLOCK";
    private static final String UNBLOCK = "UNBLOCK";
    private static final String DELETE = "DELETE";
    private static final String UNDELETE = "UNDELETE";

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void addAdminUser(CreateOrUpdateAdminRequestDto requestDto, Long maintainerUserId) {
        UserValidations.validateAdminCreationRequestByMaintainer(requestDto);
        UserValidations.validateExistingUserCheck(requestDto, userRepository);
        UserAuthModel user = new UserAuthModel();
        user.setUsername(requestDto.getUsername().trim());
        user.setPassword(StringConstants.encoder.encode(requestDto.getPassword().trim()));
        user.setOtpCount(0);
        user.setDeleted(Boolean.FALSE);
        user.setBlocked(Boolean.FALSE);
        user.setRoleId(getUserRoleIdBasedOnRoleName(UserRoles.ADMIN.name()));
        user.setLoginCodeValue(LoginMode.MAINTAINER_CREATION.getLoginProcessCode());
        userAuthHistRepository.save(new UserAuthHist(userRepository.save(user).getId(), LocalDateTime.now(), reasonCodeIdAssociation.get(ADMIN_CREATION), requestDto.getComment(), maintainerUserId));
    }

    @Override
    public List<AdminUsersResponseDto> getAdminUsersList(String type) {
        return userRepository.findByRoleId(getUserRoleIdBasedOnRoleName(UserRoles.ADMIN.name())).stream()
                .filter(user -> {
                    if (BLOCK.equalsIgnoreCase(type)) return user.isBlocked();
                    else if (DELETE.equalsIgnoreCase(type)) return user.isDeleted();
                    else return (!user.isBlocked() && !user.isDeleted());
                })
                .map(user -> AdminUsersResponseDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .build())
                .toList();
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void updateAdminUser(CreateOrUpdateAdminRequestDto requestDto, Long adminUserId, Long maintainerUserId) {
        UserAuthModel user = userRepository.findById(adminUserId).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        if (!Objects.equals(user.getUsername(), requestDto.getUsername().trim()))
            UserValidations.validateExistingUserCheck(requestDto, userRepository);
        sessionTokenRepository.deleteAllByUsername(user.getUsername());
        user.setUsername(requestDto.getUsername().trim());
        user.setPassword(encoder.encode(requestDto.getPassword().trim()));
        userAuthHistRepository.save(new UserAuthHist(userRepository.save(user).getId(), LocalDateTime.now(), reasonCodeIdAssociation.get(ADMIN_UPDATE), requestDto.getComment(), maintainerUserId));
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void deleteAdminUser(Long adminUserId, Long maintainerUserId, String type) {
        UserAuthModel user = userRepository.findById(adminUserId).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        if (BLOCK.equalsIgnoreCase(type))
            user.setBlocked(Boolean.TRUE);
        else if (DELETE.equalsIgnoreCase(type))
            user.setDeleted(Boolean.TRUE);
        sessionTokenRepository.deleteAllByUsername(user.getUsername());
        userAuthHistRepository.save(new UserAuthHist(userRepository.save(user).getId(), LocalDateTime.now(), DELETE.equalsIgnoreCase(type) ? reasonCodeIdAssociation.get(ADMIN_DELETE) : reasonCodeIdAssociation.get(ADMIN_BLOCK), "Maintainer " + type + "ED the account", maintainerUserId));
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void unblockOrRetrieveAdmin(Long adminUserId, Long maintainerUserId, String type) {
        UserAuthModel user = userRepository.findById(adminUserId).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        if (UNBLOCK.equalsIgnoreCase(type))
            user.setBlocked(Boolean.FALSE);
        else if (UNDELETE.equalsIgnoreCase(type))
            user.setDeleted(Boolean.FALSE);
        userAuthHistRepository.save(new UserAuthHist(userRepository.save(user).getId(), LocalDateTime.now(), UNDELETE.equalsIgnoreCase(type) ? reasonCodeIdAssociation.get(ADMIN_RETRIEVAL) : reasonCodeIdAssociation.get(ADMIN_UNBLOCK), "Maintainer " + type + "ED the account", maintainerUserId));
    }

    private int getUserRoleIdBasedOnRoleName(String role){
        int roleId = 0;
        for (Map.Entry<Integer, String> it : userRoleAssociation.entrySet()) {
            if (it.getValue().equalsIgnoreCase(role)) {
                roleId = it.getKey();
            }
        }
        return roleId;
    }
}
