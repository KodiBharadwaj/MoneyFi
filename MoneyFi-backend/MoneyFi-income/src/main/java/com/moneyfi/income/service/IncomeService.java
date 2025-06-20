package com.moneyfi.income.service;

import com.moneyfi.income.service.dto.response.AccountStatementDto;
import com.moneyfi.income.service.dto.response.IncomeDeletedDto;
import com.moneyfi.income.model.IncomeModel;
import com.moneyfi.income.service.dto.response.IncomeDetailsDto;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
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

    List<AccountStatementDto> getAccountStatementOfUser(Long userId, LocalDate fromDate, LocalDate toDate);

    void generatePdfForAccountStatement(Long userId, LocalDate fromDate, LocalDate toDate, HttpServletResponse response) throws IOException;

    void sendAccountStatementEmailToUser(Long userId, LocalDate fromDate, LocalDate toDate, HttpServletResponse response);
}
