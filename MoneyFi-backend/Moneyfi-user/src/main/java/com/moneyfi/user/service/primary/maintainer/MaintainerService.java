package com.moneyfi.user.service.primary.maintainer;

import com.moneyfi.constants.enums.UserRoles;
import com.moneyfi.user.service.primary.maintainer.dto.request.CreateOrUpdateSupportUserCreationRequestDto;
import com.moneyfi.user.service.primary.maintainer.dto.response.SupportUsersResponseDto;

import java.util.List;

public interface MaintainerService {
    void addUsersByMaintainer(CreateOrUpdateSupportUserCreationRequestDto requestDto, Long maintainerUserId);

    List<SupportUsersResponseDto> getSupportUsersList(String type, UserRoles role);

    void updateSupportUser(CreateOrUpdateSupportUserCreationRequestDto requestDto, Long supportUserId, Long maintainerUserId);

    void unblockOrRetrieveSupportUser(Long supportUserId, Long maintainerUserId, String type);

    void blockOrDeleteSupportUser(Long adminUserId, Long maintainerUserId, String type);

    void registerAgent(Long maintainerId, Long agentId);
}
