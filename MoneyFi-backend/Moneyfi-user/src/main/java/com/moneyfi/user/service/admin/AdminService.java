package com.moneyfi.user.service.admin;

import com.moneyfi.user.service.admin.dto.request.AdminScheduleRequestDto;
import com.moneyfi.user.service.admin.dto.request.ReasonDetailsRequestDto;
import com.moneyfi.user.service.admin.dto.request.ReasonUpdateRequestDto;
import com.moneyfi.user.service.admin.dto.request.ScheduleNotificationRequestDto;
import com.moneyfi.user.service.admin.dto.response.*;
import com.moneyfi.user.service.common.dto.response.UserFeedbackResponseDto;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface AdminService {

    AdminOverviewPageDto getAdminOverviewPageDetails();

    List<UserRequestsGridDto> getUserRequestsGridForAdmin(String status);

    List<UserDefectResponseDto> getUserRaisedDefectsForAdmin(String status);

    void updateDefectStatus(Long defectId, String status, String reason, Long adminUserId);

    boolean accountReactivationAndNameChangeRequest(String email, String referenceNumber, String requestStatus, Long adminUserId, String approveStatus, String declineReason, int gmailSyncRequestCount);

    String blockTheUserAccountByAdmin(String email, String reason, MultipartFile file, Long adminUserId);

    List<UserGridDto> getUserDetailsGridForAdmin(String status);

    byte[] getUserDetailsExcelForAdmin(String status);

    Map<Integer, Integer> getUserMonthlyCountInAYear(int year, String status);

    UserProfileAndRequestDetailsDto getCompleteUserDetailsForAdmin(String username);

    List<UserFeedbackResponseDto> getUserFeedbackListForAdmin();

    void updateUserFeedback(Long feedbackId, Long adminUserId);

    void addReasonsForUserReasonDialog(ReasonDetailsRequestDto reason, Long adminUserId);

    List<ReasonListResponseDto> getAllReasonsBasedOnReasonCode(int reasonCode);

    void updateReasonsForUserReasonDialogByReasonCode(ReasonUpdateRequestDto requestDto, Long adminUserId);

    void deleteReasonByReasonId(int reasonId, Long adminUserId);

    Map<String, List<UserDefectHistDetailsResponseDto>> getUserDefectHistDetails(List<Long> defectIds);

    List<String> getUsernamesOfAllUsers();

    void scheduleNotification(@Valid ScheduleNotificationRequestDto requestDto, Long adminUserId);

    List<AdminSchedulesResponseDto> getAllActiveSchedulesOfAdmin(String status);

    void cancelTheUserScheduling(Long scheduleId, Long adminUserId);

    void updateAdminPlacedSchedules(@Valid AdminScheduleRequestDto requestDto, Long adminUserId);

    void deleteUserScheduling(Long scheduleId, Long adminUserId);
}
