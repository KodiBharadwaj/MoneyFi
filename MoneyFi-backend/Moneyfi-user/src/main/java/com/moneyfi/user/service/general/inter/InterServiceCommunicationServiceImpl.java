package com.moneyfi.user.service.general.inter;

import com.moneyfi.constants.dto.BatchInfoForEmailDto;
import com.moneyfi.constants.enums.NotificationQueueEnum;
import com.moneyfi.user.repository.auth.UserRepository;
import com.moneyfi.user.repository.general.ProfileRepository;
import com.moneyfi.user.service.user.dto.internal.NotificationQueueDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.moneyfi.user.util.constants.StringConstants.functionToGetNameOfUserWithUserId;
import static com.moneyfi.user.util.constants.StringConstants.objectMapper;

@Service
@RequiredArgsConstructor
public class InterServiceCommunicationServiceImpl implements InterServiceCommunicationService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    @Async
    @Override
    public void sendBatchInformationEmailToUser(List<BatchInfoForEmailDto> batchInfoList) {
        for (BatchInfoForEmailDto dto : batchInfoList) {
            dto.setName(functionToGetNameOfUserWithUserId(profileRepository, dto.getUserId()));
            dto.setUsername(userRepository.findById(dto.getUserId()).get().getUsername());
        }
        try {
            applicationEventPublisher.publishEvent(
                    new NotificationQueueDto(
                            NotificationQueueEnum.USER_BATCH_INFO_EMAIL.name(),
                            objectMapper.writeValueAsString(batchInfoList)
                    )
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
