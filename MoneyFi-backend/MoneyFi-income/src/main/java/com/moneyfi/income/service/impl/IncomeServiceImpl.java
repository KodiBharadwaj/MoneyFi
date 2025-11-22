package com.moneyfi.income.service.impl;

import com.moneyfi.income.exceptions.ResourceNotFoundException;
import com.moneyfi.income.exceptions.ScenarioNotPossibleException;
import com.moneyfi.income.service.IncomeService;
import com.moneyfi.income.service.dto.request.AccountStatementRequestDto;
import com.moneyfi.income.service.dto.request.IncomeSaveRequest;
import com.moneyfi.income.service.dto.request.IncomeUpdateRequest;
import com.moneyfi.income.service.dto.response.*;
import com.moneyfi.income.model.IncomeDeleted;
import com.moneyfi.income.model.IncomeModel;
import com.moneyfi.income.repository.IncomeDeletedRepository;
import com.moneyfi.income.repository.IncomeRepository;
import com.moneyfi.income.repository.common.IncomeCommonRepository;
import com.moneyfi.income.utils.GeneratePdfTemplate;
import com.moneyfi.income.utils.IncomeValidator;
import com.moneyfi.income.utils.StringConstants;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.moneyfi.income.utils.StringConstants.*;

@Slf4j
@Service
public class IncomeServiceImpl implements IncomeService {

    private final IncomeRepository incomeRepository;
    private final IncomeCommonRepository incomeCommonRepository;
    private final IncomeDeletedRepository incomeDeletedRepository;
    private final GeneratePdfTemplate generatePdfTemplate;
    private final RestTemplate restTemplate;

    public IncomeServiceImpl(IncomeRepository incomeRepository,
                             IncomeCommonRepository incomeCommonRepository,
                             IncomeDeletedRepository incomeDeletedRepository,
                             GeneratePdfTemplate generatePdfTemplate,
                             RestTemplate restTemplate){
        this.incomeRepository = incomeRepository;
        this.incomeCommonRepository = incomeCommonRepository;
        this.incomeDeletedRepository = incomeDeletedRepository;
        this.generatePdfTemplate = generatePdfTemplate;
        this.restTemplate = restTemplate;
    }

    private static final String INCOME_ALREADY_PRESENT_MESSAGE = "Income with this source and category is already there";

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void saveIncome(IncomeSaveRequest incomeSaveRequest, Long userId) {
        IncomeValidator.validateIncomeSaveRequest(incomeSaveRequest);
        IncomeModel incomeModel = incomeRepository.getIncomeBySourceAndCategory(userId, incomeSaveRequest.getSource().trim(), incomeSaveRequest.getCategory().trim(), LocalDateTime.parse(incomeSaveRequest.getDate()));
        if(incomeModel != null){
            throw new ScenarioNotPossibleException(INCOME_ALREADY_PRESENT_MESSAGE);
        }
        IncomeModel income = new IncomeModel();
        BeanUtils.copyProperties(incomeSaveRequest, income);
        income.setDate(LocalDateTime.parse(incomeSaveRequest.getDate()));
        income.setUserId(userId);
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
    public List<IncomeDetailsDto> getAllIncomesByMonthYearAndCategory(Long userId, int month, int year, String category, boolean deleteStatus) {
        return incomeCommonRepository.getAllIncomesByDate(userId, month, year, category, deleteStatus);
    }

    @Override
    public byte[] generateMonthlyExcelReport(Long userId, int month, int year, String category) {
        List<IncomeDetailsDto> monthlyIncomeList = getAllIncomesByMonthYearAndCategory(userId, month, year, category, false);
        if (monthlyIncomeList.isEmpty()) {
            throw new ResourceNotFoundException(INCOME_NOT_FOUND);
        }
        return generateExcelReport(monthlyIncomeList);
    }

    @Override
    public List<IncomeDeletedDto> getDeletedIncomesInAMonth(Long userId, int month, int year) {
        return incomeCommonRepository.getDeletedIncomesInAMonth(userId, month, year);
    }

    private byte[] generateExcelReport(List<IncomeDetailsDto> incomeList){
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
    public List<IncomeDetailsDto> getAllIncomesByYear(Long userId, int year, String category, boolean deleteStatus) {
        return incomeCommonRepository.getAllIncomesByYear(userId, year, category, deleteStatus);
    }

    @Override
    public byte[] generateYearlyExcelReport(Long userId, int year, String category) {
        List<IncomeDetailsDto> yearlyIncomeList = getAllIncomesByYear(userId, year, category, false);
        if(yearlyIncomeList.isEmpty()){
            throw new ResourceNotFoundException(INCOME_NOT_FOUND);
        }
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
        LocalDateTime currentTime = LocalDateTime.now();
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
    public List<AccountStatementResponseDto> getAccountStatementOfUser(Long userId, AccountStatementRequestDto inputDto) {
        if(inputDto.getFromDate().isAfter(inputDto.getToDate())){
            throw new IllegalArgumentException("From date should be less than To date");
        }
        AtomicInteger i = new AtomicInteger(1);
        return incomeCommonRepository.getAccountStatementOfUser(userId, inputDto)
                .stream()
                .peek(transaction -> {
                    transaction.setTransactionTime(
                            StringConstants.changeTransactionTimeToTwelveHourFormat(transaction.getTransactionTime())
                    );
                    transaction.setId(i.getAndIncrement());
                }).toList();
    }

    @Override
    public byte[] generatePdfForAccountStatement(Long userId, AccountStatementRequestDto inputDto) throws IOException {
        inputDto.setThreshold(-1); /** to get all the transactions without pagination **/
        UserDetailsForStatementDto userDetails = incomeCommonRepository.getUserDetailsForAccountStatement(userId);
        userDetails.setUsername(makeUsernamePrivate(userDetails.getUsername()));
        return generatePdfTemplate.generatePdf(getAccountStatementOfUser(userId, inputDto), userDetails, inputDto.getFromDate(), inputDto.getToDate(), generateDocumentPasswordForUser(userDetails));
    }

    private String makeUsernamePrivate(String username){
        int index = username.indexOf('@');
        return username.substring(0, index/3) + "x".repeat(index - index/3) + username.substring(index);
    }

    private String generateDocumentPasswordForUser(UserDetailsForStatementDto userDetails){
        return userDetails.getName().substring(0,4).toUpperCase() + userDetails.getUsername().substring(0,4).toLowerCase();
    }

    @Override
    public ResponseEntity<String> sendAccountStatementEmailToUser(Long userId, AccountStatementRequestDto inputDto, String token) {
        try {
            byte[] pdfBytes = generatePdfForAccountStatement(userId, inputDto);
            apiCallToGatewayServiceToSendEmail(pdfBytes, token);
            return ResponseEntity.ok("Email sent successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send email: " + e.getMessage());
        }
    }

    @Override
    public OverviewPageDetailsDto getOverviewPageTileDetails(Long userId, int month, int year) {
        return incomeCommonRepository.getOverviewPageTileDetails(userId, month, year);
    }

    @Override
    public List<Object[]> getTotalIncomeInSpecifiedRange(Long userId, LocalDateTime fromDate, LocalDateTime toDate) {
        return incomeRepository.getTotalIncomeInSpecifiedRange(userId, fromDate, toDate);
    }

    private void apiCallToGatewayServiceToSendEmail(byte[] pdfBytes, String authHeader){
        String token = authHeader.substring(7);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setBearerAuth(token);
        HttpEntity<byte[]> requestEntity = new HttpEntity<>(pdfBytes, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                StringConstants.ACCOUNT_STATEMENT_USER_SERVICE_URL,
                HttpMethod.POST,
                requestEntity,
                Void.class
        );
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new ResourceNotFoundException("Failed to send email: " + response.getStatusCode());
        }
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void updateBySource(Long id, Long userId, IncomeUpdateRequest incomeUpdateRequest) {
        IncomeModel incomeModel = incomeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(INCOME_NOT_FOUND));
        IncomeValidator.validateIncomeUpdateRequest(incomeUpdateRequest);
        if(incomeModel.getAmount().compareTo(incomeUpdateRequest.getAmount()) == 0 &&
                incomeModel.getSource().equals(incomeUpdateRequest.getSource()) &&
                incomeModel.getCategory().equals(incomeUpdateRequest.getCategory()) &&
                incomeModel.getDate().toLocalDate().equals(LocalDateTime.parse(incomeUpdateRequest.getDate()).toLocalDate()) &&
                incomeModel.isRecurring() == incomeUpdateRequest.getRecurring()){
            throw new ScenarioNotPossibleException(NO_CHANGES_TO_UPDATE);
        }
        incomeModel.setAmount(incomeUpdateRequest.getAmount());
        incomeModel.setSource(incomeUpdateRequest.getSource());
        incomeModel.setCategory(incomeUpdateRequest.getCategory());
        incomeModel.setDate(LocalDateTime.parse(incomeUpdateRequest.getDate()));
        incomeModel.setRecurring(incomeUpdateRequest.getRecurring());
        incomeModel.setUpdatedAt(LocalDateTime.now());
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
