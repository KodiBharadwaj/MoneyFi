package com.moneyfi.user.service.primary.maintainer.impl;

import com.moneyfi.constants.enums.LoginMode;
import com.moneyfi.constants.enums.ReasonEnum;
import com.moneyfi.constants.enums.UserRoles;
import com.moneyfi.user.exceptions.ResourceNotFoundException;
import com.moneyfi.user.model.primary.auth.UserAuthHist;
import com.moneyfi.user.model.primary.auth.UserAuthModel;
import com.moneyfi.user.repository.primary.auth.UserAuthHistRepository;
import com.moneyfi.user.repository.primary.auth.SessionTokenRepository;
import com.moneyfi.user.repository.primary.auth.UserRepository;
import com.moneyfi.user.service.primary.maintainer.MaintainerService;
import com.moneyfi.user.service.primary.maintainer.dto.request.CreateOrUpdateSupportUserCreationRequestDto;
import com.moneyfi.user.service.primary.maintainer.dto.response.SupportUsersResponseDto;
import com.moneyfi.user.util.constants.StringConstants;
import com.moneyfi.user.util.enums.AccDeactivationType;
import com.moneyfi.user.validator.UserValidations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

import static com.moneyfi.constants.constants.CommonConstants.reasonCodeIdAssociation;
import static com.moneyfi.constants.constants.CommonConstants.userRoleAssociation;
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
    public void addUsersByMaintainer(CreateOrUpdateSupportUserCreationRequestDto requestDto, Long maintainerUserId) {
        UserValidations.validateUserCreationRequestByMaintainer(requestDto);
        UserValidations.validateExistingUserCheck(requestDto, userRepository);
        UserAuthModel user = new UserAuthModel();
        user.setUsername(requestDto.getUsername().trim());
        user.setPassword(StringConstants.encoder.encode(requestDto.getPassword().trim()));
        user.setOtpCount(0);
        user.setDeleted(Boolean.FALSE);
        user.setBlocked(Boolean.FALSE);
        user.setRoleId(StringConstants.functionToGetRoleIdBasedOnRoleName(requestDto.getRole().name()));
        user.setLoginCodeValue(LoginMode.MAINTAINER_CREATION.getLoginProcessCode());
        userAuthHistRepository.save(new UserAuthHist(userRepository.save(user).getId(), LocalDateTime.now(ZoneId.systemDefault()), reasonCodeIdAssociation.get(requestDto.getRole() == UserRoles.ADMIN ? ADMIN_CREATION : CUSTOMER_CARE_CREATION), requestDto.getComment(), maintainerUserId));
    }

    @Override
    public List<SupportUsersResponseDto> getSupportUsersList(String type, UserRoles role) {
        return userRepository.findByRoleId(StringConstants.functionToGetRoleIdBasedOnRoleName(role.name())).stream()
                .filter(user -> {
                    if (AccDeactivationType.BLOCK.name().equalsIgnoreCase(type)) return user.isBlocked();
                    else if (AccDeactivationType.DELETE.name().equalsIgnoreCase(type)) return user.isDeleted();
                    else return (!user.isBlocked() && !user.isDeleted());
                })
                .map(user -> SupportUsersResponseDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .build())
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSupportUser(CreateOrUpdateSupportUserCreationRequestDto requestDto, Long supportUserId, Long maintainerUserId) {
        UserAuthModel user = userRepository.findById(supportUserId).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        if (!Objects.equals(user.getUsername(), requestDto.getUsername().trim()))
            UserValidations.validateExistingUserCheck(requestDto, userRepository);
        sessionTokenRepository.deleteAllByUsername(user.getUsername());
        user.setUsername(requestDto.getUsername().trim());
        user.setPassword(encoder.encode(requestDto.getPassword().trim()));
        userAuthHistRepository.save(new UserAuthHist(userRepository.save(user).getId(), LocalDateTime.now(ZoneId.systemDefault()), reasonCodeIdAssociation.get(requestDto.getRole() == UserRoles.ADMIN ? ADMIN_UPDATE : CUSTOMER_CARE_UPDATE), requestDto.getComment(), maintainerUserId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unblockOrRetrieveSupportUser(Long supportUserId, Long maintainerUserId, String type) {
        UserAuthModel user = userRepository.findById(supportUserId).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        if (AccDeactivationType.UNBLOCK.name().equalsIgnoreCase(type))
            user.setBlocked(Boolean.FALSE);
        else if (AccDeactivationType.UNDELETE.name().equalsIgnoreCase(type))
            user.setDeleted(Boolean.FALSE);

        boolean isAdmin = userRoleAssociation.get(user.getRoleId()).equalsIgnoreCase(UserRoles.ADMIN.name());
        boolean isCustomerCareAgent = userRoleAssociation.get(user.getRoleId()).equalsIgnoreCase(UserRoles.CUSTOMER_CARE.name());
        boolean isUndelete = type.equalsIgnoreCase(AccDeactivationType.UNDELETE.name());

        ReasonEnum reasonKey;
        if (isAdmin) {
            reasonKey = isUndelete ? ADMIN_RETRIEVAL : ADMIN_UNBLOCK;
        } else if (isCustomerCareAgent) {
            reasonKey = isUndelete ? CUSTOMER_CARE_RETRIEVAL : CUSTOMER_CARE_UNBLOCK;
        } else {
            throw new IllegalStateException("User has no role permitted to perform this action");
        }
        Integer reasonTypeId = reasonCodeIdAssociation.get(reasonKey);
        userAuthHistRepository.save(new UserAuthHist(userRepository.save(user).getId(), LocalDateTime.now(ZoneId.systemDefault()), reasonTypeId, "Maintainer " + type + "ED the account", maintainerUserId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSupportUser(Long supportUserId, Long maintainerUserId, String type) {
        UserAuthModel user = userRepository.findById(supportUserId).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        if (AccDeactivationType.BLOCK.name().equalsIgnoreCase(type))
            user.setBlocked(Boolean.TRUE);
        else if (AccDeactivationType.DELETE.name().equalsIgnoreCase(type))
            user.setDeleted(Boolean.TRUE);

        boolean isAdmin = userRoleAssociation.get(user.getRoleId()).equalsIgnoreCase(UserRoles.ADMIN.name());
        boolean isCustomerCareAgent = userRoleAssociation.get(user.getRoleId()).equalsIgnoreCase(UserRoles.CUSTOMER_CARE.name());
        boolean isDelete = type.equalsIgnoreCase(AccDeactivationType.DELETE.name());

        ReasonEnum reasonKey;
        if (isAdmin) {
            reasonKey = isDelete ? ADMIN_DELETE : ADMIN_BLOCK;
        } else if (isCustomerCareAgent) {
            reasonKey = isDelete ? CUSTOMER_CARE_DELETE : CUSTOMER_CARE_BLOCK;
        } else {
            throw new IllegalStateException("User has no role permitted to perform this action");
        }
        Integer reasonTypeId = reasonCodeIdAssociation.get(reasonKey);
        sessionTokenRepository.deleteAllByUsername(user.getUsername());
        userAuthHistRepository.save(new UserAuthHist(userRepository.save(user).getId(), LocalDateTime.now(ZoneId.systemDefault()), reasonTypeId, "Maintainer " + type + "ED the account", maintainerUserId));
    }
}
