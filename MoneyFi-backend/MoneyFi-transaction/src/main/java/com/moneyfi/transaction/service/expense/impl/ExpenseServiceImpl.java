package com.moneyfi.transaction.service.expense.impl;

import com.moneyfi.transaction.exceptions.ResourceNotFoundException;
import com.moneyfi.transaction.exceptions.ScenarioNotPossibleException;
import com.moneyfi.transaction.model.expense.ExpenseModel;
import com.moneyfi.transaction.repository.expense.ExpenseRepository;
import com.moneyfi.transaction.repository.transaction.TransactionRepository;
import com.moneyfi.transaction.service.expense.ExpenseService;
import com.moneyfi.transaction.service.expense.response.ExpenseDetailsDto;
import com.moneyfi.transaction.utils.TransactionServiceType;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.moneyfi.transaction.utils.StringConstants.CATEGORY_ID_INVALID;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final TransactionRepository transactionRepository;

    public ExpenseServiceImpl(ExpenseRepository expenseRepository,
                              TransactionRepository transactionRepository){
        this.expenseRepository = expenseRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public ExpenseModel save(ExpenseModel expense) {
        List<Integer> categoryIds = transactionRepository.getCategoryIdsBasedOnTransactionType(TransactionServiceType.EXPENSE.name());
        if(!categoryIds.contains(expense.getCategoryId())) {
            throw new ScenarioNotPossibleException(CATEGORY_ID_INVALID);
        }
        expense.setDeleted(false);
        return expenseRepository.save(expense);
    }

    @Override
    public List<ExpenseModel> getAllExpenses(Long userId) {
        return expenseRepository.findExpensesByUserId(userId)
                .stream()
                .filter(i -> !i.isDeleted())
                .sorted((a,b) -> Long.compare(a.getId(), b.getId()))
                .toList();
    }

    @Override
    public List<ExpenseDetailsDto> getAllExpensesByMonthYearAndCategory(Long userId, int month, int year, String category, boolean deleteStatus) {
        return transactionRepository.getAllExpensesByDate(userId, month, year, category, deleteStatus);
    }

    @Override
    public byte[] generateMonthlyExcelReport(Long userId, int month, int year, String category) {
        List<ExpenseDetailsDto> monthlyExpenseList = getAllExpensesByMonthYearAndCategory(userId, month, year, category,false);
        return generateExcelReport(monthlyExpenseList);
    }

    private byte[] generateExcelReport(List<ExpenseDetailsDto> expenseList){
        try(Workbook workbook = new XSSFWorkbook()){
            Sheet sheet = workbook.createSheet("Monthly Expense Report");

            // Create Header Row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Category", "Description", "Amount", "Date", "Recurring"};
            for(int i=0; i< headers.length; i++){
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(createHeaderStyle(workbook));
            }

            // Create a Date Style
            CellStyle dateStyle = createDateStyle(workbook);

            // Populate Data Rows
            int rowIndex = 1;
            for (ExpenseDetailsDto data : expenseList) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(data.getCategory());
                row.createCell(1).setCellValue(data.getDescription());
                row.createCell(2).setCellValue(data.getAmount().doubleValue());
                // Format Date Properly
                Cell dateCell = row.createCell(3);
                dateCell.setCellValue(data.getDate()); // Assuming data.getDate() is `java.util.Date`
                dateCell.setCellStyle(dateStyle); // Apply formatting

                row.createCell(4).setCellValue(data.isRecurring()?"Yes":"No");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Convert to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
            throw new ResourceNotFoundException("Error in creating the Excel Report");
        }
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle dateStyle = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy")); // Change format as needed
        return dateStyle;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();

        font.setBold(true);
        style.setFont(font);

        // Set Background Color
        style.setFillForegroundColor(IndexedColors.YELLOW.getIndex()); // Yellow background
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND); // Apply solid fill

        // Set Border (Optional)
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    @Override
    public List<ExpenseDetailsDto> getAllExpensesByYearAndCategory(Long userId, int year, String category, boolean deleteStatus) {
        return transactionRepository.getAllExpensesByYear(userId, year, category, deleteStatus);
    }

    @Override
    public byte[] generateYearlyExcelReport(Long userId, int year, String category) {
        List<ExpenseDetailsDto> yearlyIncomeList = getAllExpensesByYearAndCategory(userId, year, category,false);
        return generateExcelReport(yearlyIncomeList);
    }

    @Override
    public List<BigDecimal> getMonthlyExpenses(Long userId, int year) {
        List<Object[]> rawExpenses = expenseRepository.findMonthlyExpenses(userId, year, false);
        BigDecimal[] monthlyTotals = new BigDecimal[12];
        Arrays.fill(monthlyTotals, BigDecimal.ZERO); // Initialize all months to 0

        for (Object[] raw : rawExpenses) {
            int month = ((Integer) raw[0]) - 1; // Months are 1-based, array is 0-based
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

    private BigDecimal[] getMonthlyIncomesListInAYear(Long userId, int year) {
        List<Object[]> rawIncomes = expenseRepository.getMonthlyIncomesListInAYear(userId, year, false);
        BigDecimal[] monthlyTotals = new BigDecimal[12];
        Arrays.fill(monthlyTotals, BigDecimal.ZERO); // Initialize all months to 0

        for (Object[] raw : rawIncomes) {
            int month = ((Integer) raw[0]) - 1; // Months are 1-based, array is 0-based
            BigDecimal total = (BigDecimal) raw[1];
            monthlyTotals[month] = total;
        }

        return monthlyTotals;
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

    private BigDecimal getTotalIncomeInMonthAndYear(Long userId, int month, int year) {
        BigDecimal totalIncome = expenseRepository.getTotalIncomeInMonthAndYear(userId, month, year);
        if(totalIncome == null){
            return BigDecimal.ZERO;
        }

        return totalIncome;
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
    @Transactional
    public ResponseEntity<ExpenseDetailsDto> updateBySource(Long id, Long userId, ExpenseModel expense) {
        expense.setUserId(userId);
        expense.setDeleted(false);

        ExpenseModel expenseModel = expenseRepository.findById(id).orElse(null);
        List<Integer> categoryIds = transactionRepository.getCategoryIdsBasedOnTransactionType(TransactionServiceType.EXPENSE.name());
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
                expenseModel.isRecurring() == expense.isRecurring()){
            return ResponseEntity.noContent().build(); // 204
        }

        if(expense.getCategoryId() != null){
            expenseModel.setCategoryId(expense.getCategoryId());
        }
        if(expense.getAmount().compareTo(BigDecimal.ZERO) > 0){
            expenseModel.setAmount(expense.getAmount());
        }
        if(expense.getDate() != null){
            expenseModel.setDate(expense.getDate());
        }
        if(expense.getDescription() != null){
            expenseModel.setDescription(expense.getDescription());
        }
        if(expense.isRecurring()){
            expenseModel.setRecurring(expense.isRecurring());
        }
        expenseModel.setUpdatedAt(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CREATED).body(updateExpenseDtoConversion(save(expenseModel)));
    }

    private ExpenseDetailsDto updateExpenseDtoConversion(ExpenseModel updatedExpense){
        ExpenseDetailsDto expenseDetailsDto = new ExpenseDetailsDto();
        BeanUtils.copyProperties(updatedExpense, expenseDetailsDto);
        expenseDetailsDto.setDate(Date.valueOf(updatedExpense.getDate().toLocalDate()));
        return expenseDetailsDto;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public boolean deleteExpenseById(List<Long> ids) {
        LocalDateTime currentTime = LocalDateTime.now();
        try {
            for(Long it : ids){
                ExpenseModel expense = expenseRepository.findById(it).orElse(null);
                if(expense != null){
                    expense.setDeleted(true);
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
}
