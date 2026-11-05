package com.moneyfi.user.repository.primary.admin.impl;

import com.moneyfi.user.exceptions.ResourceNotFoundException;
import com.moneyfi.user.model.primary.auth.UserAuthModel;
import com.moneyfi.user.model.primary.auth.UserSessionTime;
import com.moneyfi.user.repository.primary.admin.UserRequestsService;
import com.moneyfi.user.repository.primary.auth.UserRepository;
import com.moneyfi.user.repository.primary.auth.UserSessionTimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static com.moneyfi.user.util.constants.StringConstants.USER_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRequestsServiceImpl implements UserRequestsService {

    private final UserSessionTimeRepository userSessionTimeRepository;
    private final UserRepository userRepository;

    @Override
    public void changeUserSessionTime(Long adminUserId, String username, Integer time) {
        UserAuthModel user = userRepository.getUserDetailsByUsername(username).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        LocalDateTime currentTime = LocalDateTime.now(ZoneId.systemDefault());
        userSessionTimeRepository.save(userSessionTimeRepository.findByUserId(user.getId())
                .map(existing ->
                        existing.toBuilder()
                                .sessionTime(time)
                                .updatedBy(adminUserId)
                                .updatedAt(currentTime)
                                .build())
                .orElseGet(() ->
                        UserSessionTime.builder()
                                .userId(user.getId())
                                .sessionTime(time)
                                .createdBy(adminUserId)
                                .createdAt(currentTime)
                                .updatedBy(adminUserId)
                                .updatedAt(currentTime)
                                .build()));
    }
}
