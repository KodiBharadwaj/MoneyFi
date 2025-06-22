package com.moneyfi.apigateway.repository.common;

import com.moneyfi.apigateway.service.common.dto.ProfileDetailsDto;

public interface CommonServiceRepository {
    ProfileDetailsDto getProfileDetailsOfUser(Long userId);
}
