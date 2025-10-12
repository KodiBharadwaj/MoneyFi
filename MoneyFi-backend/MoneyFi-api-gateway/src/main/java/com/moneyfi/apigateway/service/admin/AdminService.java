package com.moneyfi.apigateway.service.admin;

import com.moneyfi.apigateway.service.admin.dto.request.AdminScheduleRequestDto;
import com.moneyfi.apigateway.service.admin.dto.request.ReasonDetailsRequestDto;
import com.moneyfi.apigateway.service.admin.dto.request.ReasonUpdateRequestDto;
import com.moneyfi.apigateway.service.admin.dto.request.ScheduleNotificationRequestDto;
import com.moneyfi.apigateway.service.admin.dto.response.*;
import com.moneyfi.apigateway.service.common.dto.response.UserFeedbackResponseDto;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface AdminService {
    AdminOverviewPageDto getAdminOverviewPageDetails();

    List<UserRequestsGridDto> getUserRequestsGridForAdmin(String status);

    List<UserDefectResponseDto> getUserRaisedDefectsForAdmin(String status);

    void updateDefectStatus(Long defectId, String status);

    List<UserGridDto> getUserDetailsGridForAdmin(String status);

    byte[] getUserDetailsExcelForAdmin(String status);

    boolean accountReactivationAndNameChangeRequest(String email, String referenceNumber, String requestStatus, Long adminUserId, String approveStatus, String declineReason);

    Map<Integer, Integer> getUserMonthlyCountInAYear(int year, String status);

    UserProfileAndRequestDetailsDto getCompleteUserDetailsForAdmin(String username);

    String scheduleNotification(@Valid ScheduleNotificationRequestDto requestDto);

    List<UserFeedbackResponseDto> getUserFeedbackListForAdmin();

    void updateUserFeedback(Long feedbackId);

    List<String> getUsernamesOfAllUsers();

    List<AdminSchedulesResponseDto> getAllActiveSchedulesOfAdmin();

    void cancelTheUserScheduling(Long scheduleId);

    void updateAdminPlacedSchedules(@Valid AdminScheduleRequestDto requestDto);

    void addReasonsForUserReasonDialog(ReasonDetailsRequestDto reason);

    List<ReasonListResponseDto> getAllReasonsBasedOnReasonCode(int reasonCode);

    void updateReasonsForUserReasonDialogByReasonCode(ReasonUpdateRequestDto requestDto);

    void deleteReasonByReasonId(int reasonId);

    String blockTheUserAccountByAdmin(String email, String reason, MultipartFile file, Long adminUserId);

    Map<String, List<UserDefectHistDetailsResponseDto>> getUserDefectHistDetails(List<Long> defectIds);
}
