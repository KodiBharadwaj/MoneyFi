package com.moneyfi.apigateway.service.maintainer;

import com.moneyfi.apigateway.service.maintainer.dto.request.CreateOrUpdateAdminRequestDto;
import com.moneyfi.apigateway.service.maintainer.dto.response.AdminUsersResponseDto;
import jakarta.validation.Valid;

import java.util.List;

public interface MaintainerService {
    void addAdminUser(@Valid CreateOrUpdateAdminRequestDto requestDto, Long maintainerUserId);

    List<AdminUsersResponseDto> getAdminUsersList(String type);

    void updateAdminUser(@Valid CreateOrUpdateAdminRequestDto requestDto, Long adminUserId, Long maintainerUserId);

    void deleteAdminUser(Long adminUserId, Long maintainerUserId, String type);

    void unblockOrRetrieveAdmin(Long adminUserId, Long maintainerUserId, String type);
}
