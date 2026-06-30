package com.moneyfi.transaction.service.expense.impl;

import com.moneyfi.constants.constants.CommonConstants;
import com.moneyfi.constants.dto.ExcelResponseDto;
import com.moneyfi.constants.dto.GoalExpenseRelationRequestDto;
import com.moneyfi.constants.dto.excel.ExcelStreamRequestDto;
import com.moneyfi.constants.enums.TransactionServiceType;
import com.moneyfi.constants.service.ExcelGenerationService;
import com.moneyfi.transaction.dto.export.ExpenseDetailsGridExportDto;
import com.moneyfi.transaction.dto.export.IncomeDetailsGridExportDto;
import com.moneyfi.transaction.exceptions.ResourceNotFoundException;
import com.moneyfi.transaction.exceptions.ScenarioNotPossibleException;
import com.moneyfi.transaction.model.expense.ExpenseGoalRelation;
import com.moneyfi.transaction.model.expense.ExpenseModel;
import com.moneyfi.transaction.repository.expense.ExpenseGoalRelationRepository;
import com.moneyfi.transaction.repository.expense.ExpenseRepository;
import com.moneyfi.transaction.service.expense.ExpenseService;
import com.moneyfi.transaction.service.expense.dto.response.ExpenseDetailsDto;
import com.moneyfi.transaction.service.income.dto.request.TransactionsListRequestDto;
import com.moneyfi.transaction.service.transaction.TransactionService;
import com.moneyfi.transaction.utils.enums.EntryModeEnum;
import com.moneyfi.transaction.validator.TransactionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static com.moneyfi.transaction.utils.constants.StringConstants.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    @Value("${excel.export.output-dir:./exports}")
    private String outputDirectory;

    private final ExpenseRepository expenseRepository;
    private final TransactionService transactionService;
    private final ExpenseGoalRelationRepository expenseGoalRelationRepository;
    private final ExcelGenerationService excelGenerationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExpenseModel save(ExpenseModel expense) {
        List<Integer> categoryIds = transactionService.getCategoryIdsBasedOnTransactionType(TransactionServiceType.EXPENSE.name());
        if(!categoryIds.contains(expense.getCategoryId())) {
            throw new ScenarioNotPossibleException(CATEGORY_ID_INVALID);
        }
        expense.setIsDeleted(Boolean.FALSE);
        expense.setEntryMode(EntryModeEnum.MANUAL.name());
        return expenseRepository.save(expense);
    }

    @Override
    @Transactional
    public void addGoalExpenseTransaction(GoalExpenseRelationRequestDto requestDto, Long userId) {
        log.info("checking dto: {}", requestDto);
        ExpenseModel savedExpense = save(ExpenseModel.builder()
                .userId(userId)
                .categoryId(requestDto.getCategoryId())
                .amount(requestDto.getAmount())
                .date(requestDto.getDate())
                .recurring(requestDto.isRecurring())
                .description(requestDto.getDescription())
                .build()
        );
        log.info("Expense saved with id: {}", savedExpense.getId());

        ExpenseGoalRelation expenseGoalRelation = ExpenseGoalRelation.builder()
                .expense(savedExpense)
                .goalId(requestDto.getGoalId())
                .build();
        expenseGoalRelationRepository.save(expenseGoalRelation);
    }

    @Override
    public List<ExpenseModel> getAllExpenses(Long userId) {
        return expenseRepository.findExpensesByUserId(userId)
                .stream()
                .filter(i -> !i.getIsDeleted())
                .sorted((a,b) -> Long.compare(a.getId(), b.getId()))
                .toList();
    }

    @Override
    public List<ExpenseDetailsDto> getAllExpensesByDate(Long userId, TransactionsListRequestDto requestDto) {
        TransactionValidator.validateTransactionsListGetRequestDto(userId, requestDto);
        List<ExpenseDetailsDto> expenses = transactionService.getAllExpensesByDate(userId, requestDto);
        if (requestDto.getSortBy() == null || requestDto.getSortOrder() == null) {
            return expenses;
        }

        Comparator<ExpenseDetailsDto> comparator;
        switch (requestDto.getSortBy().toLowerCase()) {
            case CATEGORY:
                comparator = Comparator.comparing(ExpenseDetailsDto::getCategory, Comparator.nullsLast(String::compareToIgnoreCase));
                break;

            case DATE:
                comparator = Comparator.comparing(ExpenseDetailsDto::getDate, Comparator.nullsLast(java.util.Date::compareTo));
                break;

            case AMOUNT:
                comparator = Comparator.comparing(ExpenseDetailsDto::getAmount, Comparator.nullsLast(BigDecimal::compareTo));
                break;

            case DESCRIPTION:
                comparator = Comparator.comparing(ExpenseDetailsDto::getDescription, Comparator.nullsLast(String::compareToIgnoreCase));
                break;

            case TYPE:
                comparator = Comparator.comparing(ExpenseDetailsDto::isRecurring, Comparator.nullsLast(Boolean::compareTo));
                break;

            default: return expenses;
        }
        if (DESC.equalsIgnoreCase(requestDto.getSortOrder())) {
            comparator = comparator.reversed();
        }
        return expenses.stream().sorted(comparator).toList();
    }

    @Override
    public ExcelResponseDto getExpenseReportExcel(Long userId, TransactionsListRequestDto requestDto) throws IOException {
        List<ExpenseDetailsDto> expenseList = getAllExpensesByDate(userId, requestDto);
        if (expenseList.isEmpty()) {
            throw new ResourceNotFoundException(EXPENSE_DETAILS_NOT_FOUND);
        }

        String fileName = CommonConstants.functionToGenerateFileNameForReports("Expense-details-grid", LocalDateTime.now());

        Path outputPath = CommonConstants.prepareOutputPath(fileName, outputDirectory);

        try (OutputStream outputStream = Files.newOutputStream(outputPath);
             Stream<ExpenseDetailsGridExportDto> stream =
                     expenseList.stream().map(dto ->
                             ExpenseDetailsGridExportDto.builder()
                                     .id(dto.getId())
                                     .category(dto.getCategory())
                                     .amount(dto.getAmount())
                                     .date(dto.getDate())
                                     .recurring(dto.isRecurring())
                                     .description(dto.getDescription())
                                     .deleted(dto.isDeleted())
                                     .build()
                     )
        ) {
            ExcelStreamRequestDto<ExpenseDetailsGridExportDto> request = ExcelStreamRequestDto.<ExpenseDetailsGridExportDto>builder()
                    .fileName(fileName)
                    .sheetName("Expense Details Report")
                    .classType(ExpenseDetailsGridExportDto.class)
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
    public List<BigDecimal> getMonthlyExpenses(Long userId, int year) {
        List<Object[]> rawExpenses = expenseRepository.findMonthlyExpenses(userId, year, false);
        BigDecimal[] monthlyTotals = new BigDecimal[12];
        Arrays.fill(monthlyTotals, BigDecimal.ZERO);

        for (Object[] raw : rawExpenses) {
            int month = ((Integer) raw[0]) - 1;
            BigDecimal total = (BigDecimal) raw[1];
            monthlyTotals[month] = total;
        }
        return Arrays.asList(monthlyTotals);
    }

    @Override
    public List<BigDecimal> getMonthlySavingsList(Long userId, int year) {
        BigDecimal[] incomes = getMonthlyIncomesListInAYear(userId, year);
        List<BigDecimal> expenseList = getMonthlyExpenses(userId, year);
        BigDecimal[] expenses = expenseList.toArray(new BigDecimal[0]);

        List<BigDecimal> savings = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            savings.add(incomes[i].subtract(expenses[i]));
        }
        return savings;
    }

    @Override
    public BigDecimal getTotalExpenseInMonthAndYear(Long userId, int month, int year) {
        BigDecimal totalExpense = expenseRepository.getTotalExpenseInMonthAndYear(userId, month, year);
        if(totalExpense == null){
            return BigDecimal.ZERO;
        }
        return totalExpense;
    }

    @Override
    public BigDecimal getTotalSavingsByMonthAndDate(Long userId, int month, int year) {
        BigDecimal totalIncome = getTotalIncomeInMonthAndYear(userId, month, year);
        BigDecimal totalExpenses = getTotalExpenseInMonthAndYear(userId, month, year);

        if(totalIncome.compareTo(totalExpenses) > 0){
            return totalIncome.subtract(totalExpenses);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public List<BigDecimal> getCumulativeMonthlySavings(Long userId, int year) {

        BigDecimal[] incomes = getMonthlyIncomesListInAYear(userId, year);
        BigDecimal[] expenses = getMonthlyExpenses(userId, year).toArray(new BigDecimal[0]);

        LocalDate currentDate = LocalDate.now();
        int currentYear = currentDate.getYear();
        int currentMonth = currentDate.getMonthValue();

        if(year > currentYear) return Arrays.asList(new BigDecimal[12]);

        int lastMonth = (year < currentYear) ? 12 : currentMonth;

        List<BigDecimal> savings = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            if(i < lastMonth){
                savings.add(incomes[i].subtract(expenses[i]));
            }
            else{
                savings.add(BigDecimal.ZERO);
            }
        }

        List<BigDecimal> cumulativeSavings = new ArrayList<>();
        cumulativeSavings.add(savings.get(0));
        for(int i=1; i<12; i++){
            if(i < lastMonth){
                cumulativeSavings.add(cumulativeSavings.get(i-1).add(savings.get(i)));
            }
            else {
                cumulativeSavings.add(BigDecimal.ZERO);
            }
        }
        return cumulativeSavings;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<ExpenseDetailsDto> updateBySource(Long id, Long userId, ExpenseModel expense) {
        expense.setUserId(userId);
        expense.setIsDeleted(Boolean.FALSE);

        ExpenseModel expenseModel = expenseRepository.findById(id).orElse(null);

        List<Integer> categoryIds = transactionService.getCategoryIdsBasedOnTransactionType(TransactionServiceType.EXPENSE.name());
        if(!categoryIds.contains(expense.getCategoryId())) {
            throw new ScenarioNotPossibleException(CATEGORY_ID_INVALID);
        }

        if(expenseModel == null || !expenseModel.getUserId().equals(userId)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        if(expenseModel.getAmount().compareTo(expense.getAmount()) == 0 &&
                expenseModel.getCategoryId().equals(expense.getCategoryId()) &&
                expenseModel.getDescription().equals(expense.getDescription()) &&
                expenseModel.getDate().equals(expense.getDate()) &&
                expenseModel.getRecurring() == expense.getRecurring()){
            return ResponseEntity.noContent().build(); // 204
        }

        if(expense.getCategoryId() != null){
            expenseModel.setCategoryId(expense.getCategoryId());
        }
        if(expense.getAmount().compareTo(BigDecimal.ZERO) > 0){
            expenseModel.setAmount(expense.getAmount());
        }
        if(expense.getDate() != null && !expense.getDate().toLocalDate().equals(expenseModel.getDate().toLocalDate())){
            expenseModel.setDate(expense.getDate());
        }
        if(expense.getDescription() != null){
            expenseModel.setDescription(expense.getDescription());
        }
        if(expense.getRecurring()){
            expenseModel.setRecurring(expense.getRecurring());
        }
        expenseModel.setUpdatedAt(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CREATED).body(updateExpenseDtoConversion(expenseRepository.save(expenseModel)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteExpenseById(List<Long> ids) {
        LocalDateTime currentTime = LocalDateTime.now();
        try {
            for(Long it : ids){
                ExpenseModel expense = expenseRepository.findById(it).orElse(null);
                if(expense != null){
                    expense.setIsDeleted(Boolean.TRUE);
                    expense.setUpdatedAt(currentTime);
                    expenseRepository.save(expense);
                }
            }
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Object[]> getTotalExpensesInSpecifiedRange(Long userId, LocalDateTime fromDate, LocalDateTime toDate) {
        return expenseRepository.getTotalIncomeInSpecifiedRange(userId, fromDate, toDate);
    }

    private BigDecimal[] getMonthlyIncomesListInAYear(Long userId, int year) {
        List<Object[]> rawIncomes = expenseRepository.getMonthlyIncomesListInAYear(userId, year, false);
        BigDecimal[] monthlyTotals = new BigDecimal[12];
        Arrays.fill(monthlyTotals, BigDecimal.ZERO);

        for (Object[] raw : rawIncomes) {
            int month = ((Integer) raw[0]) - 1;
            BigDecimal total = (BigDecimal) raw[1];
            monthlyTotals[month] = total;
        }
        return monthlyTotals;
    }

    private BigDecimal getTotalIncomeInMonthAndYear(Long userId, int month, int year) {
        BigDecimal totalIncome = expenseRepository.getTotalIncomeInMonthAndYear(userId, month, year);
        if(totalIncome == null){
            return BigDecimal.ZERO;
        }
        return totalIncome;
    }

    private ExpenseDetailsDto updateExpenseDtoConversion(ExpenseModel updatedExpense){
        ExpenseDetailsDto expenseDetailsDto = new ExpenseDetailsDto();
        BeanUtils.copyProperties(updatedExpense, expenseDetailsDto);
        expenseDetailsDto.setDate(Date.valueOf(updatedExpense.getDate().toLocalDate()));
        return expenseDetailsDto;
    }
}
