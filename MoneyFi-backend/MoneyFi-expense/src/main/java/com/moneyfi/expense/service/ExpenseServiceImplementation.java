package com.moneyfi.expense.service;

import com.moneyfi.expense.model.ExpenseModel;
import com.moneyfi.expense.repository.ExpenseRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ExpenseServiceImplementation implements ExpenseService{

    @Autowired
    private RestTemplate restTemplate;

    private final ExpenseRepository expenseRepository;

    public ExpenseServiceImplementation(ExpenseRepository expenseRepository){
        this.expenseRepository = expenseRepository;
    }

    @Override
    public ExpenseModel save(ExpenseModel expense) {
        expense.set_deleted(false);
        return expenseRepository.save(expense);
    }

    @Override
    public List<ExpenseModel> getAllexpenses(int userId) {
        return expenseRepository.findExpensesByUserId(userId).stream().filter(i->i.is_deleted() == false).toList();
    }

    @Override
    public List<ExpenseModel> getAllexpensesByDate(int userId, int month, int year, boolean deleteStatus) {
        return expenseRepository.getAllexpensesByDate(userId, month, year, deleteStatus);
    }

    @Override
    public byte[] generateMonthlyExcelReport(int userId, int month, int year) {
        List<ExpenseModel> monthlyExpenseList = expenseRepository.getAllexpensesByDate(userId, month, year, false);

        return generateExcelReport(monthlyExpenseList);
    }

    private byte[] generateExcelReport(List<ExpenseModel> expenseList){
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
            for (ExpenseModel data : expenseList) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(data.getCategory());
                row.createCell(1).setCellValue(data.getDescription());
                row.createCell(2).setCellValue(data.getAmount());
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
    public List<ExpenseModel> getAllexpensesByYear(int userId, int year, boolean deleteStatus) {
        return expenseRepository.getAllexpensesByYear(userId, year, deleteStatus);
    }

    @Override
    public byte[] generateYearlyExcelReport(int userId, int year) {
        List<ExpenseModel> yearlyIncomeList = expenseRepository.getAllexpensesByYear(userId, year, false);

        return generateExcelReport(yearlyIncomeList);
    }

    @Override
    public List<Double> getMonthlyExpenses(int userId, int year) {
        List<Object[]> rawExpenses = expenseRepository.findMonthlyExpenses(userId, year, false);
        Double[] monthlyTotals = new Double[12];
        Arrays.fill(monthlyTotals, 0.0); // Initialize all months to 0

        for (Object[] raw : rawExpenses) {
            int month = ((Integer) raw[0]) - 1; // Months are 1-based, array is 0-based
            double total = (Double) raw[1];
            monthlyTotals[month] = total;
        }

        return Arrays.asList(monthlyTotals);
    }

    @Override
    public Double getTotalExpensesUpToPreviousMonth(int userId, int month, int year) {
        // Adjust month and year to point to the previous month
        final int adjustedMonth;
        final int adjustedYear;

        if (month == 1) { // Handle January case
            adjustedMonth = 13;
            adjustedYear = year - 1;
        } else {
            adjustedMonth = month;
            adjustedYear = year;
        }
        Double value = expenseRepository.getTotalExpensesUpToPreviousMonth(userId, adjustedMonth, adjustedYear);
        if(value != null){
            return value;
        } else {
            return 0.0;
        }
    }

    @Override
    public Double getTotalExpenseInMonthAndYear(int userId, int month, int year) {
        Double totalExpense = expenseRepository.getTotalExpenseInMonthAndYear(userId, month, year);
        if(totalExpense == null) return 0.0;

        return totalExpense;
    }

    @Override
    public Double getTotalSavingsByMonthAndDate(int userId, int month, int year) {
        Double totalIncome = restTemplate.getForObject("http://MONEYFI-INCOME/api/income/" + userId + "/totalIncome/" + month + "/" + year, Double.class);
        Double totalExpenses = getTotalExpenseInMonthAndYear(userId, month, year);
        if(totalIncome > totalExpenses){
            return (totalIncome - totalExpenses);
        }

        return 0.0;
    }

    @Override
    public List<Double> getCumulativeMonthlySavings(int userId, int year) {

        Double[] incomes = restTemplate.getForObject("http://MONEYFI-INCOME/api/income/"+userId+"/monthlyTotalIncomesList/"+year,Double[].class);
        Double[] expenses = getMonthlyExpenses(userId, year).toArray(new Double[0]);
        LocalDate currentDate = LocalDate.now();
        int currentYear = currentDate.getYear();
        int currentMonth = currentDate.getMonthValue();

        if(year > currentYear) return Arrays.asList(new Double[12]);

        int lastMonth = (year < currentYear) ? 12 : currentMonth;

        List<Double> savings = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            if(i < lastMonth){
                savings.add(incomes[i] - expenses[i]);
            }
            else{
                savings.add(0.0);
            }
        }

        List<Double> cumulativeSavings = new ArrayList<>();
        cumulativeSavings.add(savings.get(0));
        for(int i=1; i<12; i++){
            if(i < lastMonth){
                cumulativeSavings.add(cumulativeSavings.get(i-1)+savings.get(i));
            }
            else {
                cumulativeSavings.add(0.0);
            }
        }
        return cumulativeSavings;
    }

    @Override
    public ExpenseModel updateBySource(int id, ExpenseModel expense) {
        ExpenseModel expenseModel = expenseRepository.findById(id).orElse(null);

        if(expense.getCategory() != null){
            expenseModel.setCategory(expense.getCategory());
        }
        if(expense.getAmount() > 0){
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

        return save(expenseModel);
    }

    @Override
    public boolean deleteExpenseById(int id) {

        try {
            ExpenseModel expense = expenseRepository.findById(id).orElse(null);
            expense.set_deleted(true);
            expenseRepository.save(expense);
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }

    }
}
