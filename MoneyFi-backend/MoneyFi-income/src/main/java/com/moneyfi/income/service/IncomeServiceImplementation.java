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
    private RestTemplate restTemplate;

    private final IncomeRepository incomeRepository;

    public IncomeServiceImplementation(IncomeRepository incomeRepository){
        this.incomeRepository = incomeRepository;
    }

    @Override
    public IncomeModel save(IncomeModel income) {
        IncomeModel incomeModel = incomeRepository.getIncomeBySourceAndCategory(income.getUserId(), income.getSource(), income.getCategory(), income.getDate());

        if(incomeModel != null){
            return null;
        }

        income.set_deleted(false);
        return incomeRepository.save(income);
    }

    @Override
    public List<IncomeModel> getAllIncomes(Long userId) {
        return incomeRepository.findIncomesOfUser(userId)
                .stream()
                .filter(i->i.is_deleted()==false)
                .sorted((a,b) -> Long.compare(a.getId(), b.getId()))
                .toList();
    }

    @Override
    public List<IncomeModel> getAllIncomesByMonthYearAndCategory(Long userId, int month, int year, String category, boolean deleteStatus) {
        List<IncomeModel> list = incomeRepository.getAllIncomesByDate(userId, month, year, deleteStatus);
        if(category.equalsIgnoreCase("all")){
            return list.stream()
                    .sorted((a,b) -> Long.compare(a.getId(), b.getId()))
                    .toList();
        }

        return list.stream()
                .filter(i -> i.getCategory().equalsIgnoreCase(category))
                .sorted((a,b) -> Long.compare(a.getId(), b.getId()))
                .toList();
    }

    @Override
    public byte[] generateMonthlyExcelReport(Long userId, int month, int year, String category) {

        List<IncomeModel> monthlyIncomeList = getAllIncomesByMonthYearAndCategory(userId, month, year, category,false);
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
    public List<IncomeModel> getAllIncomesByYear(Long userId, int year, String category, boolean deleteStatus) {
        List<IncomeModel> list = incomeRepository.getAllIncomesByYear(userId, year, deleteStatus);
        if(category.equalsIgnoreCase("all")){
            return list.stream()
                    .sorted((a,b) -> Long.compare(a.getId(), b.getId()))
                    .toList();
        }

        return list.stream()
                .filter(i -> i.getCategory().equalsIgnoreCase(category))
                .sorted((a,b) -> Long.compare(a.getId(), b.getId()))
                .toList();
    }

    @Override
    public byte[] generateYearlyExcelReport(Long userId, int year, String category) {

        List<IncomeModel> yearlyIncomeList = getAllIncomesByYear(userId, year, category, false);
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

        String url = "http://MONEYFI-EXPENSE/api/expense/" + userId + "/totalExpensesUpToPreviousMonth/" + month +"/" + year;

        BigDecimal totalExpense = restTemplate.getForObject(url, BigDecimal.class);
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

        String url = "http://MONEYFI-EXPENSE/api/expense/" + incomeModel.getUserId() + "/totalExpense/" +
                                    incomeModel.getDate().getMonthValue() + "/" + incomeModel.getDate().getYear();
        BigDecimal totalExpensesInMonth = restTemplate.getForObject(url, BigDecimal.class);

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
        String url = "http://MONEYFI-EXPENSE/api/expense/" + incomeModel.getUserId() + "/totalExpense/" +
                                    incomeModel.getDate().getMonthValue() + "/" + incomeModel.getDate().getYear();

        BigDecimal totalExpensesInMonth = restTemplate.getForObject(url, BigDecimal.class);

        if(currentNetIncome.compareTo(totalExpensesInMonth) > 0){
            return true;
        }

        return false;
    }

    @Override
    public IncomeModel updateBySource(Long id, IncomeModel income) {

        IncomeModel incomeModel = incomeRepository.findById(id).orElse(null);

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

        income.set_deleted(false);
        return incomeRepository.save(income);
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
