package com.moneyfi.apigateway.repository.admin;

import com.moneyfi.apigateway.service.admin.dto.response.*;

import java.util.List;

public interface AdminRepository {
    List<AdminSchedulesResponseDto> getAllActiveSchedulesOfAdmin();
}
