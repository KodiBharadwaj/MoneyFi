package com.moneyfi.user.service.general.inter;

import com.moneyfi.constants.dto.BatchInfoForEmailDto;

import java.util.List;

public interface InterServiceCommunicationService {
    void sendBatchInformationEmailToUser(List<BatchInfoForEmailDto> batchInfoList);
}
