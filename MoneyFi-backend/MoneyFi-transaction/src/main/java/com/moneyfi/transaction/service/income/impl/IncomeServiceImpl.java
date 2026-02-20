package com.moneyfi.transaction.service.income.impl;

import com.moneyfi.transaction.exceptions.ResourceNotFoundException;
import com.moneyfi.transaction.exceptions.ScenarioNotPossibleException;
import com.moneyfi.transaction.repository.transaction.TransactionRepository;
import com.moneyfi.transaction.service.income.IncomeService;
import com.moneyfi.transaction.service.income.dto.request.IncomeSaveRequest;
import com.moneyfi.transaction.service.income.dto.request.IncomeUpdateRequest;
import com.moneyfi.transaction.model.income.IncomeDeleted;
import com.moneyfi.transaction.model.income.IncomeModel;
import com.moneyfi.transaction.repository.income.IncomeDeletedRepository;
import com.moneyfi.transaction.repository.income.IncomeRepository;
import com.moneyfi.transaction.service.income.dto.request.TransactionsListRequestDto;
import com.moneyfi.transaction.service.income.dto.response.*;
import com.moneyfi.transaction.utils.enums.EntryModeEnum;
import com.moneyfi.transaction.utils.enums.TransactionServiceType;
import com.moneyfi.transaction.validator.IncomeValidator;
import com.moneyfi.transaction.validator.TransactionValidator;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static com.moneyfi.transaction.utils.StringConstants.*;

@Slf4j
@Service
public class IncomeServiceImpl implements IncomeService {

    private final IncomeRepository incomeRepository;
    private final TransactionRepository transactionRepository;
    private final IncomeDeletedRepository incomeDeletedRepository;

    public IncomeServiceImpl(IncomeRepository incomeRepository,
                             TransactionRepository transactionRepository,
                             IncomeDeletedRepository incomeDeletedRepository){
        this.incomeRepository = incomeRepository;
        this.transactionRepository = transactionRepository;
        this.incomeDeletedRepository = incomeDeletedRepository;
    }

    private static final String INCOME_ALREADY_PRESENT_MESSAGE = "Income with this source and category is already there";

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void saveIncome(IncomeSaveRequest incomeSaveRequest, Long userId) {
        IncomeValidator.validateIncomeSaveRequest(incomeSaveRequest, userId);
        List<Integer> categoryIds = transactionRepository.getCategoryIdsBasedOnTransactionType(TransactionServiceType.INCOME.name());
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
                .filter(i -> !i.isDeleted())
                .sorted((a,b) -> a.getDate().compareTo(b.getDate()))
                .toList();
    }

    @Override
    public List<IncomeDetailsDto> getAllIncomesByDate(Long userId, TransactionsListRequestDto requestDto) {
        TransactionValidator.validateTransactionsListGetRequestDto(userId, requestDto);
        List<IncomeDetailsDto> incomes = transactionRepository.getAllIncomesByDate(userId, requestDto);
        if (requestDto.getSortBy() == null || requestDto.getSortOrder() == null) {
            return incomes;
        }

        Comparator<IncomeDetailsDto> comparator;
        switch (requestDto.getSortBy().toLowerCase()) {
            case CATEGORY:
                comparator = Comparator.comparing(IncomeDetailsDto::getCategory, Comparator.nullsLast(String::compareToIgnoreCase));
                break;

            case DATE:
                comparator = Comparator.comparing(IncomeDetailsDto::getDate, Comparator.nullsLast(java.util.Date::compareTo));
                break;

            case AMOUNT:
                comparator = Comparator.comparing(IncomeDetailsDto::getAmount, Comparator.nullsLast(BigDecimal::compareTo));
                break;

            case SOURCE:
                comparator = Comparator.comparing(IncomeDetailsDto::getSource, Comparator.nullsLast(String::compareTo));
                break;

            case TYPE:
                comparator = Comparator.comparing(IncomeDetailsDto::isRecurring, Comparator.nullsLast(Boolean::compareTo));
                break;

            default: return incomes;
        }
        if (DESC.equalsIgnoreCase(requestDto.getSortOrder())) {
            comparator = comparator.reversed();
        }
        return incomes.stream().sorted(comparator).toList();
    }

    @Override
    public byte[] getIncomesReportExcel(Long userId, TransactionsListRequestDto requestDto) {
        List<IncomeDetailsDto> monthlyIncomeList = getAllIncomesByDate(userId, requestDto);
        if (monthlyIncomeList.isEmpty()) {
            throw new ResourceNotFoundException(INCOME_NOT_FOUND);
        }
        return generateExcelReport(monthlyIncomeList);
    }

    @Override
    public List<IncomeDeletedDto> getDeletedIncomesInAMonth(Long userId, int month, int year) {
        return transactionRepository.getDeletedIncomesInAMonth(userId, month, year);
    }

    private byte[] generateExcelReport(List<IncomeDetailsDto> incomeList){
        try(Workbook workbook = new XSSFWorkbook()){
            Sheet sheet = workbook.createSheet("Monthly Income Report");

            // Create Header Row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Category", "Source", "Amount", "Date", "Recurring", "Description"};
            for(int i=0; i< headers.length; i++){
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(createHeaderStyle(workbook));
            }

            // Create a Date Style
            CellStyle dateStyle = createDateStyle(workbook);

            // Populate Data Rows
            int rowIndex = 1;
            for (IncomeDetailsDto data : incomeList) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(data.getCategory());
                row.createCell(1).setCellValue(data.getSource());
                row.createCell(2).setCellValue(data.getAmount().doubleValue());
                // Format Date Properly
                Cell dateCell = row.createCell(3);
                dateCell.setCellValue(data.getDate()); // Assuming data.getDate() is `java.util.Date`
                dateCell.setCellStyle(dateStyle); // Apply formatting

                row.createCell(4).setCellValue(data.isRecurring() ? YES : NO);
                row.createCell(5).setCellValue(data.getDescription());
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
            throw new ResourceNotFoundException(ERROR_GENERATION_EXCEL);
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
    @Transactional(rollbackOn = Exception.class)
    public boolean incomeRevertFunction(Long incomeId, Long userId) {
        IncomeDeleted incomeDeleted = incomeDeletedRepository.findByIncomeId(incomeId);
        LocalDateTime expiryTime = incomeDeleted.getExpiryDateTime();
        LocalDateTime currentTime = LocalDateTime.now();;
        Integer numberOfDays = (int) ChronoUnit.DAYS.between(currentTime.toLocalDate(), expiryTime.toLocalDate());
        if(numberOfDays > 0){
            IncomeModel income = incomeRepository.findById(incomeId).orElse(null);
            if(income != null && income.getUserId().equals(userId)){
                income.setDeleted(false);
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
    @Transactional(rollbackOn = Exception.class)
    public void updateBySource(Long id, Long userId, IncomeUpdateRequest incomeUpdateRequest) {
        IncomeModel incomeModel = incomeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(INCOME_NOT_FOUND));
        IncomeValidator.validateIncomeUpdateRequest(incomeUpdateRequest);
        List<Integer> categoryIds = transactionRepository.getCategoryIdsBasedOnTransactionType(TransactionServiceType.INCOME.name());
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
    @Transactional(rollbackOn = Exception.class)
    public boolean deleteIncomeById(Long id, Long userId) {
        try {
            IncomeModel income = incomeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(INCOME_NOT_FOUND));
            LocalDateTime currentTime = LocalDateTime.now();
            income.setUpdatedAt(currentTime);
            income.setDeleted(true);
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
