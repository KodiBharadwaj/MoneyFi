package com.moneyfi.user.service.maintainer.impl;

import com.moneyfi.constants.enums.LoginMode;
import com.moneyfi.constants.enums.UserRoles;
import com.moneyfi.user.exceptions.ResourceNotFoundException;
import com.moneyfi.user.model.auth.UserAuthHist;
import com.moneyfi.user.model.auth.UserAuthModel;
import com.moneyfi.user.repository.auth.UserAuthHistRepository;
import com.moneyfi.user.repository.auth.SessionTokenRepository;
import com.moneyfi.user.repository.auth.UserRepository;
import com.moneyfi.user.service.maintainer.MaintainerService;
import com.moneyfi.user.service.maintainer.dto.request.CreateOrUpdateAdminRequestDto;
import com.moneyfi.user.service.maintainer.dto.response.AdminUsersResponseDto;
import com.moneyfi.user.util.constants.StringConstants;
import com.moneyfi.user.util.enums.AccDeactivationType;
import com.moneyfi.user.validator.UserValidations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static com.moneyfi.constants.constants.CommonConstants.reasonCodeIdAssociation;
import static com.moneyfi.constants.enums.ReasonEnum.*;
import static com.moneyfi.user.util.constants.StringConstants.USER_NOT_FOUND;
import static com.moneyfi.user.util.constants.StringConstants.encoder;

@Service
@Slf4j
@RequiredArgsConstructor
public class MaintainerServiceImpl implements MaintainerService {

    private final UserRepository userRepository;
    private final UserAuthHistRepository userAuthHistRepository;
    private final SessionTokenRepository sessionTokenRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAdminUser(CreateOrUpdateAdminRequestDto requestDto, Long maintainerUserId) {
        UserValidations.validateAdminCreationRequestByMaintainer(requestDto);
        UserValidations.validateExistingUserCheck(requestDto, userRepository);
        UserAuthModel user = new UserAuthModel();
        user.setUsername(requestDto.getUsername().trim());
        user.setPassword(StringConstants.encoder.encode(requestDto.getPassword().trim()));
        user.setOtpCount(0);
        user.setDeleted(Boolean.FALSE);
        user.setBlocked(Boolean.FALSE);
        user.setRoleId(StringConstants.functionToGetRoleIdBasedOnRoleName((UserRoles.ADMIN.name())));
        user.setLoginCodeValue(LoginMode.MAINTAINER_CREATION.getLoginProcessCode());
        userAuthHistRepository.save(new UserAuthHist(userRepository.save(user).getId(), LocalDateTime.now(), reasonCodeIdAssociation.get(ADMIN_CREATION), requestDto.getComment(), maintainerUserId));
    }

    @Override
    public List<AdminUsersResponseDto> getAdminUsersList(String type) {
        return userRepository.findByRoleId(StringConstants.functionToGetRoleIdBasedOnRoleName((UserRoles.ADMIN.name()))).stream()
                .filter(user -> {
                    if (AccDeactivationType.BLOCK.name().equalsIgnoreCase(type)) return user.isBlocked();
                    else if (AccDeactivationType.DELETE.name().equalsIgnoreCase(type)) return user.isDeleted();
                    else return (!user.isBlocked() && !user.isDeleted());
                })
                .map(user -> AdminUsersResponseDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .build())
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
    @Transactional(rollbackFor = Exception.class)
    public void deleteAdminUser(Long adminUserId, Long maintainerUserId, String type) {
        UserAuthModel user = userRepository.findById(adminUserId).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        if (AccDeactivationType.BLOCK.name().equalsIgnoreCase(type))
            user.setBlocked(Boolean.TRUE);
        else if (AccDeactivationType.DELETE.name().equalsIgnoreCase(type))
            user.setDeleted(Boolean.TRUE);
        sessionTokenRepository.deleteAllByUsername(user.getUsername());
        userAuthHistRepository.save(new UserAuthHist(userRepository.save(user).getId(), LocalDateTime.now(), AccDeactivationType.DELETE.name().equalsIgnoreCase(type) ? reasonCodeIdAssociation.get(ADMIN_DELETE) : reasonCodeIdAssociation.get(ADMIN_BLOCK), "Maintainer " + type + "ED the account", maintainerUserId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unblockOrRetrieveAdmin(Long adminUserId, Long maintainerUserId, String type) {
        UserAuthModel user = userRepository.findById(adminUserId).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        if (AccDeactivationType.UNBLOCK.name().equalsIgnoreCase(type))
            user.setBlocked(Boolean.FALSE);
        else if (AccDeactivationType.UNDELETE.name().equalsIgnoreCase(type))
            user.setDeleted(Boolean.FALSE);
        userAuthHistRepository.save(new UserAuthHist(userRepository.save(user).getId(), LocalDateTime.now(), AccDeactivationType.UNDELETE.name().equalsIgnoreCase(type) ? reasonCodeIdAssociation.get(ADMIN_RETRIEVAL) : reasonCodeIdAssociation.get(ADMIN_UNBLOCK), "Maintainer " + type + "ED the account", maintainerUserId));
    }
}
