package com.moneyfi.apigateway.service.admin;

import com.moneyfi.apigateway.service.admin.dto.request.AdminScheduleRequestDto;
import com.moneyfi.apigateway.service.admin.dto.request.ScheduleNotificationRequestDto;
import com.moneyfi.apigateway.service.admin.dto.response.*;
import jakarta.validation.Valid;

import java.util.List;

public interface AdminService {

    String scheduleNotification(@Valid ScheduleNotificationRequestDto requestDto, String token);

    List<String> getUsernamesOfAllUsers();

    List<AdminSchedulesResponseDto> getAllActiveSchedulesOfAdmin();

    void cancelTheUserScheduling(Long scheduleId);

    void updateAdminPlacedSchedules(@Valid AdminScheduleRequestDto requestDto);
}
