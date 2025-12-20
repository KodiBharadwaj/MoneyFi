package com.moneyfi.user.validator;

import com.moneyfi.user.exceptions.ScenarioNotPossibleException;
import com.moneyfi.user.service.admin.dto.request.ScheduleNotificationRequestDto;

public class AdminValidations {

    private AdminValidations() {};

    public static void validateScheduleNotificationRequestDetails(ScheduleNotificationRequestDto requestDto) {
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
    }
}
