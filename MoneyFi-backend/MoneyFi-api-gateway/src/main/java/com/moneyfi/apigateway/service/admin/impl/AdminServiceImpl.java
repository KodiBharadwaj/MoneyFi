package com.moneyfi.apigateway.service.admin.impl;

import com.moneyfi.apigateway.exceptions.ResourceNotFoundException;
import com.moneyfi.apigateway.exceptions.ScenarioNotPossibleException;
import com.moneyfi.apigateway.model.auth.UserAuthModel;
import com.moneyfi.apigateway.model.common.*;
import com.moneyfi.apigateway.repository.admin.AdminRepository;
import com.moneyfi.apigateway.repository.user.*;
import com.moneyfi.apigateway.repository.user.auth.UserRepository;
import com.moneyfi.apigateway.service.admin.AdminService;
import com.moneyfi.apigateway.service.admin.dto.request.AdminScheduleRequestDto;
import com.moneyfi.apigateway.service.admin.dto.request.ScheduleNotificationRequestDto;
import com.moneyfi.apigateway.service.admin.dto.response.*;
import com.moneyfi.apigateway.util.constants.StringUtils;
import com.moneyfi.apigateway.util.enums.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import static com.moneyfi.apigateway.util.constants.StringUtils.*;

@Service
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final ScheduleNotificationRepository scheduleNotificationRepository;
    private final RestTemplate restTemplate;

    public AdminServiceImpl(AdminRepository adminRepository,
                            UserRepository userRepository,
                            ScheduleNotificationRepository scheduleNotificationRepository,
                            @Qualifier("getRestTemplate") RestTemplate restTemplate){
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
        this.scheduleNotificationRepository = scheduleNotificationRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    @Transactional
    public String scheduleNotification(ScheduleNotificationRequestDto requestDto, String token) {
        if(requestDto.getSubject() == null || requestDto.getSubject().isEmpty()){
            throw new ScenarioNotPossibleException("Subject can't be null or empty");
        }
        if(requestDto.getDescription() == null || requestDto.getDescription().isEmpty()){
            throw new ScenarioNotPossibleException("Description can't be null or empty");
        }
        if(requestDto.getScheduleFrom() == null || requestDto.getScheduleTo() == null){
            throw new ScenarioNotPossibleException("From and To dates should not be null");
        }
        if(requestDto.getScheduleTo().isBefore(requestDto.getScheduleFrom())){
            throw new ScenarioNotPossibleException("To Date should be greater than From Date");
        }
        if(requestDto.getRecipients() == null || requestDto.getRecipients().isEmpty()){
            throw new ScenarioNotPossibleException("Recipients should be empty");
        }
        ScheduleNotification scheduleNotification = new ScheduleNotification();
        BeanUtils.copyProperties(requestDto, scheduleNotification);
        scheduleNotification.setActive(true);
        scheduleNotification.setCancelled(false);
        scheduleNotification.setCreatedDate(LocalDateTime.now());
        ScheduleNotification response = scheduleNotificationRepository.save(scheduleNotification);

        if (!requestDto.getRecipients().equalsIgnoreCase("All")) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("recipients", requestDto.getRecipients());
            payload.put("id", response.getId());
            apiCallToUserServiceToSaveNotificationsForAllUsers(payload, token);
        } else {
            /** Currently using @Async batch process for saving notifications for all the users.
             * For more traffic of users, It is advisable to use direct insert queries.
             * Else For more efficient approach, we can use Kafka or any messaging queue to handle such huge number of people scenarios.
             */
            apiCallToUserServiceToSaveNotificationsForParticularUsers(userRepository.findAll()
                    .stream()
                    .filter(user -> !userRoleAssociation.get(user.getRoleId()).equalsIgnoreCase(UserRoles.ADMIN.name())).toList(), response.getId(), token
            );
        }
        return "Notification set successfully";
    }

    private void apiCallToUserServiceToSaveNotificationsForParticularUsers(
            List<UserAuthModel> users, Long notificationId, String token) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, Object> payload = new HashMap<>();
        payload.put("recipients", users);
        payload.put("id", notificationId);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                StringUtils.USER_SERVICE_URL_FOR_ADMIN + "/user-notifications/save-all",
                HttpMethod.POST,
                requestEntity,
                Void.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new ResourceNotFoundException("Failed to add: " + response.getStatusCode());
        }
    }

    private void apiCallToUserServiceToSaveNotificationsForAllUsers(Map<String, Object> payload, String token){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setBearerAuth(token);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);
        ResponseEntity<Void> response = restTemplate.exchange(
                StringUtils.USER_SERVICE_URL_FOR_ADMIN + "/user-notifications/save",
                HttpMethod.POST,
                requestEntity,
                Void.class
        );
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new ResourceNotFoundException("Failed to add: " + response.getStatusCode());
        }
    }

    @Override
    public List<String> getUsernamesOfAllUsers() {
        return userRepository.findAll()
                .stream()
                .filter(user -> userRoleAssociation.get(user.getRoleId()).equalsIgnoreCase(UserRoles.USER.name()))
                .map(UserAuthModel::getUsername)
                .toList();
    }

    @Override
    public List<AdminSchedulesResponseDto> getAllActiveSchedulesOfAdmin() {
        return adminRepository.getAllActiveSchedulesOfAdmin();
    }

    @Override
    @Transactional
    public void cancelTheUserScheduling(Long scheduleId) {
        ScheduleNotification notification = scheduleNotificationRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + scheduleId));
        if (Boolean.TRUE.equals(notification.isCancelled())) {
            throw new IllegalStateException("Schedule with id " + scheduleId + " is already cancelled.");
        }
        notification.setCancelled(true);
        scheduleNotificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void updateAdminPlacedSchedules(AdminScheduleRequestDto requestDto) {
        ScheduleNotification notification = scheduleNotificationRepository.findById(requestDto.getScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + requestDto.getScheduleId()));
        if(requestDto.getSubject() == null || requestDto.getSubject().isEmpty()){
            throw new ScenarioNotPossibleException("Subject can't be null or empty");
        }
        if(requestDto.getDescription() == null || requestDto.getDescription().isEmpty()){
            throw new ScenarioNotPossibleException("Description can't be null or empty");
        }
        if(requestDto.getScheduleFrom() == null || requestDto.getScheduleTo() == null){
            throw new ScenarioNotPossibleException("From and To dates should not be null");
        }
        if(requestDto.getScheduleTo().toLocalDateTime().isBefore(requestDto.getScheduleFrom().toLocalDateTime())){
            throw new ScenarioNotPossibleException("To Date should be greater than From Date");
        }
        if(requestDto.getRecipients() == null || requestDto.getRecipients().isEmpty()){
            throw new ScenarioNotPossibleException("Recipients should be empty");
        }
        notification.setSubject(requestDto.getSubject());
        notification.setDescription(requestDto.getDescription());
        notification.setScheduleFrom(requestDto.getScheduleFrom().toLocalDateTime());
        notification.setScheduleTo(requestDto.getScheduleTo().toLocalDateTime());
        notification.setRecipients(requestDto.getRecipients());
        notification.setDescription("New Update: " + notification.getDescription());
        scheduleNotificationRepository.save(notification);

//        userNotificationRepository.findByScheduleId(requestDto.getScheduleId()).forEach(userNotification -> {
//            userNotification.setRead(false);
//            userNotificationRepository.save(userNotification);
//        });
    }
}
