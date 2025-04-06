package com.moneyfi.income.service;

import com.moneyfi.income.model.IncomeModel;
import com.moneyfi.income.repository.IncomeRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;


@Slf4j
@Service
public class IncomeServiceImplementation implements IncomeService {

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private RestTemplate restTemplate;

    boolean flag = false;

    @Override
    public IncomeModel save(IncomeModel income) {
         if(flag == false){
             IncomeModel incomeModel = incomeRepository.getIncomeBySourceAndCategory(income.getUserId(), income.getSource(), income.getCategory());

             if(incomeModel != null){
                 if(incomeModel.getDate().getYear() == income.getDate().getYear() &&
                        incomeModel.getDate().getMonthValue() == income.getDate().getMonthValue()){
                     return null;
                 }
             }
             income.set_deleted(false);
             return incomeRepository.save(income);
         }
         else {
             income.set_deleted(false);
             return incomeRepository.save(income);
         }
    }

    @Override
    public List<IncomeModel> getAllIncomes(Long userId) {
        return incomeRepository.findIncomesOfUser(userId)
                .stream()
                .filter(i->i.is_deleted()==false)
                .toList();
    }

    @Override
    public List<IncomeModel> getAllIncomesByDate(Long userId, int month, int year, boolean deleteStatus) {
        return incomeRepository.getAllIncomesByDate(userId, month, year, deleteStatus);
    }

    @Override
    public byte[] generateMonthlyExcelReport(Long userId, int month, int year) {

        List<IncomeModel> monthlyIncomeList = incomeRepository.getAllIncomesByDate(userId, month, year, false);

        return generateExcelReport(monthlyIncomeList);
    }

    private byte[] generateExcelReport(List<IncomeModel> incomeList){
        try(Workbook workbook = new XSSFWorkbook()){
            Sheet sheet = workbook.createSheet("Monthly Income Report");

            // Create Header Row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Category", "Source", "Amount", "Date", "Recurring"};
            for(int i=0; i< headers.length; i++){
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(createHeaderStyle(workbook));
            }

            // Create a Date Style
            CellStyle dateStyle = createDateStyle(workbook);

            // Populate Data Rows
            int rowIndex = 1;
            for (IncomeModel data : incomeList) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(data.getCategory());
                row.createCell(1).setCellValue(data.getSource());
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
    public List<IncomeModel> getAllIncomesByYear(Long userId, int year, boolean deleteStatus) {
        return incomeRepository.getAllIncomesByYear(userId, year, deleteStatus);
    }

    @Override
    public byte[] generateYearlyExcelReport(Long userId, int year) {

        List<IncomeModel> yearlyIncomeList = incomeRepository.getAllIncomesByYear(userId, year, false);

        return generateExcelReport(yearlyIncomeList);
    }

    @Override
    public List<BigDecimal> getMonthlyIncomes(Long userId, int year) {
        List<Object[]> rawIncomes = incomeRepository.findMonthlyIncomes(userId, year, false);
        BigDecimal[] monthlyTotals = new BigDecimal[12];
        Arrays.fill(monthlyTotals, BigDecimal.ZERO); // Initialize all months to 0

        for (Object[] raw : rawIncomes) {
            int month = ((Integer) raw[0]) - 1; // Months are 1-based, array is 0-based
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
    public BigDecimal getRemainingIncomeUpToPreviousMonthByMonthAndYear(Long userId, int month, int year) {

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

        BigDecimal totalIncome = incomeRepository.getRemainingIncomeUpToPreviousMonthByMonthAndYear(userId, adjustedMonth, adjustedYear);
        if(totalIncome == null || totalIncome == BigDecimal.ZERO){
            return BigDecimal.ZERO;
        }

        BigDecimal totalExpense = restTemplate.getForObject("http://MONEYFI-EXPENSE/api/expense/" + userId + "/totalExpensesUpToPreviousMonth/" + month +"/" + year, BigDecimal.class);
        if(totalExpense.compareTo(totalIncome) > 0){
            return BigDecimal.ZERO;
        }

        return totalIncome.subtract(totalExpense);
    }

    @Override
    public boolean incomeUpdateCheckFunction(IncomeModel incomeModel) {

        BigDecimal totalIncome = getTotalIncomeInMonthAndYear(incomeModel.getUserId(), incomeModel.getDate().getMonthValue(), incomeModel.getDate().getYear());
        BigDecimal previousUpdatedIncome = incomeRepository.getIncomeByIncomeId(incomeModel.getId());
        if(previousUpdatedIncome == null){
            previousUpdatedIncome = BigDecimal.ZERO;
        }

        BigDecimal updatedIncome = incomeModel.getAmount();
        BigDecimal currentNetIncome = totalIncome.subtract(previousUpdatedIncome).add(updatedIncome);
        BigDecimal totalExpensesInMonth = restTemplate.getForObject("http://MONEYFI-EXPENSE/api/expense/" + incomeModel.getUserId() + "/totalExpense/" + incomeModel.getDate().getMonthValue() + "/" + incomeModel.getDate().getYear(), BigDecimal.class);

        if(currentNetIncome.compareTo(totalExpensesInMonth) > 0){
            return true;
        }
        return false;
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
        BigDecimal totalExpensesInMonth = restTemplate.getForObject("http://MONEYFI-EXPENSE/api/expense/" + incomeModel.getUserId() + "/totalExpense/" + incomeModel.getDate().getMonthValue() + "/" + incomeModel.getDate().getYear(), BigDecimal.class);

        if(currentNetIncome.compareTo(totalExpensesInMonth) > 0){
            return true;
        }

        return false;
    }

    @Override
    public IncomeModel updateBySource(Long id, IncomeModel income) {

        flag = true;
        IncomeModel incomeModel = incomeRepository.findById(id).orElse(null);

        if(incomeModel.getAmount() == income.getAmount() &&
                    incomeModel.getSource() == income.getSource() &&
                    incomeModel.getCategory() == income.getCategory() &&
                    incomeModel.getDate() == income.getDate() &&
                    incomeModel.isRecurring() == income.isRecurring()){
        }
        if(income.getAmount().compareTo(BigDecimal.ZERO) > 0){
            incomeModel.setAmount(income.getAmount());
        }
        if(income.getSource() != null){
            incomeModel.setSource(income.getSource());
        }
        if(income.getCategory() != null){
            incomeModel.setCategory(income.getCategory());
        }
        if(income.getDate() != null){
            incomeModel.setDate(income.getDate());
        }
        incomeModel.setRecurring(income.isRecurring());

        return save(incomeModel);
    }

    @Override
    public boolean deleteIncomeById(Long id) {

        try {
            IncomeModel income = incomeRepository.findById(id).orElse(null);
            income.set_deleted(true);
            incomeRepository.save(income);
            return true;

        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }

}
