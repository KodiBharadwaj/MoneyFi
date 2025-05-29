package com.moneyfi.expense.service;

import com.moneyfi.expense.model.ExpenseModel;
import com.moneyfi.expense.repository.ExpenseRepository;
import com.moneyfi.expense.repository.common.ExpenseCommonRepository;
import com.moneyfi.expense.service.dto.response.ExpenseDetailsDto;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ExpenseServiceImplementation implements ExpenseService{

    private final ExpenseRepository expenseRepository;
    private final ExpenseCommonRepository expenseCommonRepository;
    private final RestTemplate restTemplate;

    public ExpenseServiceImplementation(ExpenseRepository expenseRepository,
                                        RestTemplate restTemplate,
                                        ExpenseCommonRepository expenseCommonRepository){
        this.expenseRepository = expenseRepository;
        this.restTemplate = restTemplate;
        this.expenseCommonRepository = expenseCommonRepository;
    }

    @Override
    public ExpenseModel save(ExpenseModel expense) {
        expense.set_deleted(false);
        return expenseRepository.save(expense);
    }

    @Override
    public List<ExpenseModel> getAllExpenses(Long userId) {
        return expenseRepository.findExpensesByUserId(userId)
                .stream()
                .filter(i->i.is_deleted() == false)
                .sorted((a,b) -> Long.compare(a.getId(), b.getId()))
                .toList();
    }

    @Override
    public List<ExpenseDetailsDto> getAllExpensesByMonthYearAndCategory(Long userId, int month, int year, String category, boolean deleteStatus) {
        return expenseCommonRepository.getAllExpensesByDate(userId, month, year, category, deleteStatus);
    }

    @Override
    @Transactional
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
            throw new RuntimeException(e);
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
        return expenseCommonRepository.getAllExpensesByYear(userId, year, category, deleteStatus);
    }

    @Override
    @Transactional
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
    @Transactional
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
        BigDecimal totalExpense = expenseCommonRepository.getTotalExpenseInMonthAndYear(userId, month, year);
        if(totalExpense == null){
            return BigDecimal.ZERO;
        }

        return totalExpense;
    }

    @Override
    @Transactional
    public BigDecimal getTotalSavingsByMonthAndDate(Long userId, int month, int year) {
        BigDecimal totalIncome = getTotalIncomeInMonthAndYear(userId, month, year);
        BigDecimal totalExpenses = getTotalExpenseInMonthAndYear(userId, month, year);

        if(totalIncome.compareTo(totalExpenses) > 0){
            return totalIncome.subtract(totalExpenses);
        }

        return BigDecimal.ZERO;
    }
    private BigDecimal getTotalIncomeInMonthAndYear(Long userId, int month, int year) {
        BigDecimal totalIncome = expenseCommonRepository.getTotalIncomeInMonthAndYear(userId, month, year);
        if(totalIncome == null){
            return BigDecimal.ZERO;
        }

        return totalIncome;
    }

    @Override
    @Transactional
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
    public ExpenseDetailsDto updateBySource(Long id, Long userId, ExpenseModel expense) {
        expense.setUserId(userId);
        expense.set_deleted(false);

        ExpenseModel expenseModel = expenseRepository.findById(id).orElse(null);

        if(expense.getCategory() != null){
            expenseModel.setCategory(expense.getCategory());
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

        return updateExpenseDtoConversion(save(expenseModel));
    }
    private ExpenseDetailsDto updateExpenseDtoConversion(ExpenseModel updatedExpense){
        ExpenseDetailsDto expenseDetailsDto = new ExpenseDetailsDto();
        BeanUtils.copyProperties(updatedExpense, expenseDetailsDto);
        expenseDetailsDto.setDate(Date.valueOf(updatedExpense.getDate()));
        return expenseDetailsDto;
    }

    @Override
    public boolean deleteExpenseById(List<Long> ids) {

        try {
            for(Long it : ids){
                ExpenseModel expense = expenseRepository.findById(it).orElse(null);
                expense.set_deleted(true);
                expenseRepository.save(expense);
            }
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }

    }
}
