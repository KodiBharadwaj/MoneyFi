package com.moneyfi.income.service;

import com.moneyfi.income.service.dto.request.AccountStatementRequestDto;
import com.moneyfi.income.service.dto.response.AccountStatementResponseDto;
import com.moneyfi.income.service.dto.response.IncomeDeletedDto;
import com.moneyfi.income.model.IncomeModel;
import com.moneyfi.income.service.dto.response.IncomeDetailsDto;
import com.moneyfi.income.service.dto.response.OverviewPageDetailsDto;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface IncomeService {

    IncomeModel save(IncomeModel income);

    List<IncomeModel> getAllIncomes(Long userId);

    List<IncomeDetailsDto> getAllIncomesByMonthYearAndCategory(Long userId, int month, int year, String category, boolean deleteStatus);

    byte[] generateMonthlyExcelReport(Long userId, int month, int year, String category);

    List<IncomeDeletedDto> getDeletedIncomesInAMonth(Long userId, int month, int year);

    List<IncomeDetailsDto> getAllIncomesByYear(Long userId, int year, String category, boolean deleteStatus);

    byte[] generateYearlyExcelReport(Long userId, int year, String category);

    List<BigDecimal> getMonthlyIncomes(Long userId, int year);

    BigDecimal getTotalIncomeInMonthAndYear(Long userId, int month, int year);

    boolean incomeUpdateCheckFunction(IncomeModel incomeModel, Long userId);

    boolean incomeDeleteCheckFunction(IncomeModel incomeModel);

    boolean incomeRevertFunction(Long incomeId, Long userId);

    ResponseEntity<IncomeDetailsDto> updateBySource(Long id, Long userId, IncomeModel income);

    boolean deleteIncomeById(Long id, Long userId);

    BigDecimal getAvailableBalanceOfUser(Long userId);

    List<AccountStatementResponseDto> getAccountStatementOfUser(Long userId, AccountStatementRequestDto inputDto);

    byte[] generatePdfForAccountStatement(Long userId, AccountStatementRequestDto inputDto) throws IOException;

    ResponseEntity<String> sendAccountStatementEmailToUser(Long userId, AccountStatementRequestDto inputDto, String token);

    OverviewPageDetailsDto getOverviewPageTileDetails(Long userId, int month, int year);

    List<Object[]> getTotalIncomeInSpecifiedRange(Long userId, LocalDateTime fromDate, LocalDateTime toDate);
}
