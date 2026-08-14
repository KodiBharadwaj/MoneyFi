package com.moneyfi.transaction.service.income.impl;

import com.moneyfi.constants.constants.CommonConstants;
import com.moneyfi.constants.dto.ExcelResponseDto;
import com.moneyfi.constants.dto.excel.ExcelStreamRequestDto;
import com.moneyfi.constants.enums.TransactionServiceType;
import com.moneyfi.constants.service.ExcelGenerationService;
import com.moneyfi.transaction.dto.export.IncomeDetailsGridExportDto;
import com.moneyfi.transaction.exceptions.ResourceNotFoundException;
import com.moneyfi.transaction.exceptions.ScenarioNotPossibleException;
import com.moneyfi.transaction.service.income.IncomeService;
import com.moneyfi.transaction.service.income.dto.request.IncomeSaveRequest;
import com.moneyfi.transaction.service.income.dto.request.IncomeUpdateRequest;
import com.moneyfi.transaction.model.income.IncomeDeleted;
import com.moneyfi.transaction.model.income.IncomeModel;
import com.moneyfi.transaction.repository.income.IncomeDeletedRepository;
import com.moneyfi.transaction.repository.income.IncomeRepository;
import com.moneyfi.transaction.service.income.dto.request.TransactionsListRequestDto;
import com.moneyfi.transaction.service.income.dto.response.*;
import com.moneyfi.transaction.service.transaction.TransactionService;
import com.moneyfi.transaction.utils.TransactionUtilService;
import com.moneyfi.transaction.utils.enums.EntryModeEnum;
import com.moneyfi.transaction.validator.IncomeValidator;
import com.moneyfi.transaction.validator.TransactionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.moneyfi.transaction.utils.TransactionUtilService.INCOME_COMPARATORS;
import static com.moneyfi.transaction.utils.constants.StringConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncomeServiceImpl implements IncomeService {

    @Value("${excel.export.output-dir:./exports}")
    private String outputDirectory;

    private final IncomeRepository incomeRepository;
    private final IncomeDeletedRepository incomeDeletedRepository;
    private final TransactionService transactionService;
    private final ExcelGenerationService excelGenerationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveIncome(IncomeSaveRequest incomeSaveRequest, Long userId) {
        IncomeValidator.validateIncomeSaveRequest(incomeSaveRequest, userId);

        List<Integer> categoryIds = transactionService.getCategoryIdsBasedOnTransactionType(TransactionServiceType.INCOME.name());
        if(!categoryIds.contains(incomeSaveRequest.getCategoryId())) {
            throw new ScenarioNotPossibleException(CATEGORY_ID_INVALID);
        }
        IncomeModel incomeModel = incomeRepository.getIncomeBySourceAndCategory(userId, incomeSaveRequest.getSource().trim(), incomeSaveRequest.getCategoryId(), LocalDateTime.parse(incomeSaveRequest.getDate()));
        if(incomeModel != null){
            throw new ScenarioNotPossibleException(INCOME_ALREADY_PRESENT_MESSAGE);
        }
        IncomeModel income = new IncomeModel();
        BeanUtils.copyProperties(incomeSaveRequest, income);
        income.setDate(LocalDateTime.parse(incomeSaveRequest.getDate()));
        income.setUserId(userId);
        income.setEntryMode(EntryModeEnum.MANUAL.name());
        incomeRepository.save(income);
    }

    @Override
    public List<IncomeModel> getAllIncomes(Long userId) {
        return incomeRepository.findIncomesOfUser(userId)
                .stream()
                .filter(i -> !i.getIsDeleted())
                .sorted((a,b) -> a.getDate().compareTo(b.getDate()))
                .toList();
    }

    @Override
    public List<IncomeDetailsDto> getAllIncomesByDate(Long userId, TransactionsListRequestDto requestDto) {
        TransactionValidator.validateTransactionsListGetRequestDto(userId, requestDto);
        List<IncomeDetailsDto> incomes = transactionService.getAllIncomesByDate(userId, requestDto);
        if (requestDto.getSortBy() == null || requestDto.getSortOrder() == null) {
            return incomes;
        }
        return TransactionUtilService.returnSortedTransactionResponse(requestDto, incomes, INCOME_COMPARATORS);
    }

    @Override
    public ExcelResponseDto getIncomesReportExcel(Long userId, TransactionsListRequestDto requestDto) throws IOException {
        List<IncomeDetailsDto> incomesList = getAllIncomesByDate(userId, requestDto);
        if (incomesList.isEmpty()) {
            throw new ResourceNotFoundException(INCOME_DETAILS_NOT_FOUND);
        }

        String fileName = CommonConstants.functionToGenerateFileNameForReports("Income-details-grid", LocalDateTime.now());

        Path outputPath = CommonConstants.prepareOutputPath(fileName, outputDirectory);

        try (OutputStream outputStream = Files.newOutputStream(outputPath);
             Stream<IncomeDetailsGridExportDto> stream =
                     incomesList.stream().map(dto ->
                             IncomeDetailsGridExportDto.builder()
                                     .id(dto.getId())
                                     .amount(dto.getAmount())
                                     .source(dto.getSource())
                                     .date(dto.getDate())
                                     .category(dto.getCategory())
                                     .description(dto.getDescription())
                                     .entryType(dto.getEntryType())
                                     .recurring(dto.isRecurring() ? YES : NO)
                                     .build()
                     )
        ) {
            ExcelStreamRequestDto<IncomeDetailsGridExportDto> request = ExcelStreamRequestDto.<IncomeDetailsGridExportDto>builder()
                    .fileName(fileName)
                    .sheetName("Income Details Report")
                    .classType(IncomeDetailsGridExportDto.class)
                    .dataStream(stream)
                    .build();
            excelGenerationService.generateExcelReport(request, outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] excelBytes = Files.readAllBytes(outputPath);
        CommonConstants.deleteLocalFile(outputPath);
        return ExcelResponseDto.builder().excelBytes(excelBytes).excelName(fileName).build();
    }

    @Override
    public List<IncomeDeletedDto> getDeletedIncomesInAMonth(Long userId, int month, int year) {
        return transactionService.getDeletedIncomesInAMonth(userId, month, year);
    }

    @Override
    public List<BigDecimal> getMonthlyIncomes(Long userId, int year) {
        List<Object[]> rawIncomes = incomeRepository.findMonthlyIncomes(userId, year, false);
        BigDecimal[] monthlyTotals = new BigDecimal[12];
        Arrays.fill(monthlyTotals, BigDecimal.ZERO);

        for (Object[] raw : rawIncomes) {
            int month = ((Integer) raw[0]) - 1;
            BigDecimal total = (BigDecimal) raw[1];
            monthlyTotals[month] = total;
        }
        return Arrays.asList(monthlyTotals);
    }

    @Override
    public BigDecimal getTotalIncomeInMonthAndYear(Long userId, int month, int year) {
        BigDecimal totalIncome = incomeRepository.getTotalIncomeInMonthAndYear(userId, month, year);
        if(totalIncome == null){
            return BigDecimal.ZERO;
        }
        return totalIncome;
    }

    @Override
    public boolean incomeUpdateCheckFunction(IncomeModel incomeModel, Long userId) {
/**
        incomeModel.setUserId(userId);
        BigDecimal totalAvailableIncome = getAvailableBalanceOfUser(userId);
        System.out.println("totalAvailableIncome" + totalAvailableIncome);
        BigDecimal previousUpdatedIncome = incomeRepository.getIncomeByIncomeId(incomeModel.getId());
        if(previousUpdatedIncome == null){
            previousUpdatedIncome = BigDecimal.ZERO;
        }
        System.out.println("previousUpdatedIncome" + previousUpdatedIncome);
        BigDecimal updatedIncome = incomeModel.getAmount();
        BigDecimal currentNetIncome = totalAvailableIncome.subtract(previousUpdatedIncome).add(updatedIncome);
        System.out.println("currentNetIncome" + currentNetIncome);
        BigDecimal totalExpensesInMonth =
                getTotalExpenseInMonthAndYear(incomeModel.getUserId(), incomeModel.getDate().getMonthValue(), incomeModel.getDate().getYear());
        System.out.println("totalExpensesInMonth" + totalExpensesInMonth);
        if(currentNetIncome.compareTo(totalExpensesInMonth) > 0){
            return true;
        }
        return false;
**/
        return true;
    }

    private BigDecimal getTotalExpenseInMonthAndYear(Long userId, int month, int year) {
        BigDecimal totalExpense = incomeRepository.getTotalExpenseInMonthAndYear(userId, month, year);
        if(totalExpense == null){
            return BigDecimal.ZERO;
        }
        return totalExpense;
    }

    @Override
    public boolean incomeDeleteCheckFunction(IncomeModel incomeModel) {
        BigDecimal totalIncome = getTotalIncomeInMonthAndYear(incomeModel.getUserId(), incomeModel.getDate().getMonthValue(), incomeModel.getDate().getYear());
        BigDecimal previousUpdatedIncome = incomeRepository.getIncomeByIncomeId(incomeModel.getId());
        if(previousUpdatedIncome == null){
            previousUpdatedIncome = BigDecimal.ZERO;
        }

        BigDecimal updatedIncome = BigDecimal.ZERO;
        BigDecimal currentNetIncome = totalIncome.subtract(previousUpdatedIncome).add(updatedIncome);

        BigDecimal totalExpensesInMonth =
                getTotalExpenseInMonthAndYear(incomeModel.getUserId(), incomeModel.getDate().getMonthValue(), incomeModel.getDate().getYear());

        return currentNetIncome.compareTo(totalExpensesInMonth) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean incomeRevertFunction(Long incomeId, Long userId) {
        IncomeDeleted incomeDeleted = incomeDeletedRepository.findByIncomeId(incomeId);
        LocalDateTime expiryTime = incomeDeleted.getExpiryDateTime();
        LocalDateTime currentTime = LocalDateTime.now();;
        Integer numberOfDays = (int) ChronoUnit.DAYS.between(currentTime.toLocalDate(), expiryTime.toLocalDate());
        if(numberOfDays > 0){
            IncomeModel income = incomeRepository.findById(incomeId).orElse(null);
            if(income != null && income.getUserId().equals(userId)){
                income.setIsDeleted(Boolean.FALSE);
                incomeRepository.save(income);
                incomeDeletedRepository.deleteByIncomeId(incomeId);
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    @Override
    public BigDecimal getAvailableBalanceOfUser(Long userId) {
        return incomeRepository.getAvailableBalanceOfUser(userId);
    }

    @Override
    public List<Object[]> getTotalIncomeInSpecifiedRange(Long userId, LocalDateTime fromDate, LocalDateTime toDate) {
        return incomeRepository.getTotalIncomeInSpecifiedRange(userId, fromDate, toDate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBySource(Long id, Long userId, IncomeUpdateRequest incomeUpdateRequest) {
        IncomeModel incomeModel = incomeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(INCOME_DETAILS_NOT_FOUND));
        IncomeValidator.validateIncomeUpdateRequest(incomeUpdateRequest);

        List<Integer> categoryIds = transactionService.getCategoryIdsBasedOnTransactionType(TransactionServiceType.INCOME.name());
        if (!categoryIds.contains(incomeUpdateRequest.getCategoryId())) {
            throw new ScenarioNotPossibleException(CATEGORY_ID_INVALID);
        }
        incomeModel.setAmount(incomeUpdateRequest.getAmount());
        incomeModel.setSource(incomeUpdateRequest.getSource());
        incomeModel.setCategoryId(incomeUpdateRequest.getCategoryId());
        if (!incomeModel.getDate().toLocalDate().equals(LocalDateTime.parse(incomeUpdateRequest.getDate()).toLocalDate()))
            incomeModel.setDate(LocalDateTime.parse(incomeUpdateRequest.getDate()));
        incomeModel.setRecurring(incomeUpdateRequest.getRecurring());
        incomeModel.setUpdatedAt(LocalDateTime.now());
        incomeModel.setDescription(incomeUpdateRequest.getDescription());
        incomeRepository.save(incomeModel);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteIncomeById(Long id, Long userId) {
        try {
            IncomeModel income = incomeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(INCOME_DETAILS_NOT_FOUND));
            LocalDateTime currentTime = LocalDateTime.now();
            income.setUpdatedAt(currentTime);
            income.setIsDeleted(Boolean.TRUE);
            saveIncomeDeletedDetails(id, currentTime);
            incomeRepository.save(income);
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            e.printStackTrace();
            return false;
        }
    }

    private void saveIncomeDeletedDetails(Long id, LocalDateTime currentTime){
        IncomeDeleted incomeDeleted = new IncomeDeleted();
        LocalDateTime expiryTime = LocalDateTime.now().plusDays(30);
        incomeDeleted.setIncomeId(id);
        incomeDeleted.setStartDateTime(LocalDateTime.now());
        incomeDeleted.setExpiryDateTime(expiryTime);
        incomeDeleted.setDeletedAt(currentTime);
        incomeDeletedRepository.save(incomeDeleted);
    }
}
