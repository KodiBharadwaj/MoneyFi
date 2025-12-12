package com.moneyfi.transaction.service.transaction;

import com.moneyfi.transaction.service.income.dto.request.AccountStatementRequestDto;
import com.moneyfi.transaction.service.income.dto.response.AccountStatementResponseDto;
import com.moneyfi.transaction.service.income.dto.response.OverviewPageDetailsDto;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;

public interface TransactionService {
    List<AccountStatementResponseDto> getAccountStatementOfUser(Long userId, AccountStatementRequestDto inputDto);

    byte[] generatePdfForAccountStatement(Long userId, AccountStatementRequestDto inputDto) throws IOException;

    ResponseEntity<String> sendAccountStatementEmailToUser(Long userId, AccountStatementRequestDto inputDto, String token);

    OverviewPageDetailsDto getOverviewPageTileDetails(Long userId, int month, int year);
}
