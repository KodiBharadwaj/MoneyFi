package com.moneyfi.apigateway.repository.common;

import com.moneyfi.apigateway.service.common.dto.response.ProfileDetailsDto;

public interface CommonServiceRepository {
    ProfileDetailsDto getProfileDetailsOfUser(Long userId);
}
