package com.moneyfi.income.controller;

import com.moneyfi.income.config.JwtService;
import com.moneyfi.income.model.IncomeModel;
import com.moneyfi.income.service.IncomeService;
import com.moneyfi.income.service.dto.response.IncomeDeletedDto;
import com.moneyfi.income.service.dto.response.IncomeDetailsDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class IncomeApiControllerTest {

    @InjectMocks
    private IncomeApiController incomeApiControllerInject;
    @Mock
    private IncomeService incomeServiceMock;
    @Mock
    private JwtService jwtServiceMock;

    private static final Long USER_ID = 1L;
    private static final int MONTH = 5;
    private static final int YEAR = 2025;
    private static final boolean FLAG = true;
    private static final Long INCOME_ID = 7L;
    private static final String AUTH_HEADER = "Bearer fjdksajhfghdrfujrldshfgujlshrfuilhrfeujh43jt4783hgufhlag";

    @BeforeEach
    void init(){
        MockitoAnnotations.openMocks(this);
        given(jwtServiceMock.extractUserIdFromToken(AUTH_HEADER.substring(7))).willReturn(USER_ID);
    }

    @AfterEach
    void finalExec(){
        verify(jwtServiceMock, times(1)).extractUserIdFromToken(AUTH_HEADER.substring(7));
    }

    @Test
    void saveIncomeTest(){
        IncomeModel income = setIncomeModel();

        given(incomeServiceMock.save(any())).willReturn(income);

        ResponseEntity<IncomeModel> savedIncome = incomeApiControllerInject.saveIncome(income, AUTH_HEADER);

        verify(incomeServiceMock, times(1)).save(income);

        Assertions.assertNotNull(savedIncome);
        Assertions.assertEquals(HttpStatus.CREATED, savedIncome.getStatusCode());
    }

    @Test
    void saveIncomeNullTest(){
        IncomeModel income = setIncomeModel();

        given(incomeServiceMock.save(any())).willReturn(null);

        ResponseEntity<IncomeModel> savedIncome = incomeApiControllerInject.saveIncome(income, AUTH_HEADER);

        verify(incomeServiceMock, times(1)).save(income);

        Assertions.assertNotNull(savedIncome);
        Assertions.assertEquals(HttpStatus.OK, savedIncome.getStatusCode());
        Assertions.assertNull(savedIncome.getBody());
    }

    @Test
    void getAllIncomesTest(){
        List<IncomeModel> incomesList = new ArrayList<>(List.of(setIncomeModel()));

        given(incomeServiceMock.getAllIncomes(USER_ID)).willReturn(incomesList);

        ResponseEntity<List<IncomeModel>> fetchedIncomesList = incomeApiControllerInject.getAllIncomes(AUTH_HEADER);

        verify(incomeServiceMock, times(1)).getAllIncomes(USER_ID);

        Assertions.assertNotNull(fetchedIncomesList);
        Assertions.assertEquals(HttpStatus.OK, fetchedIncomesList.getStatusCode());
        Assertions.assertEquals(1, fetchedIncomesList.getBody().size());
    }

    @Test
    void getAllIncomesByMonthYearAndCategoryTest(){
        List<IncomeDetailsDto> incomesList = new ArrayList<>(List.of(setIncomeDetailsDto()));
        String category = "salary";
        boolean deleteStatus = false;

        given(incomeServiceMock
                .getAllIncomesByMonthYearAndCategory(USER_ID, MONTH, YEAR, category, deleteStatus)).willReturn(incomesList);

        ResponseEntity<List<IncomeDetailsDto>> fetchedIncomesList =
                incomeApiControllerInject.getAllIncomesByMonthYearAndCategory(AUTH_HEADER, MONTH, YEAR, category, deleteStatus);

        verify(incomeServiceMock, times(1))
                .getAllIncomesByMonthYearAndCategory(USER_ID, MONTH, YEAR, category, deleteStatus);

        Assertions.assertNotNull(fetchedIncomesList);
        Assertions.assertEquals(HttpStatus.OK, fetchedIncomesList.getStatusCode());
        Assertions.assertEquals(1, fetchedIncomesList.getBody().size());
    }

    @Test
    void getMonthlyIncomeReportTest(){
        byte[] incomesExcelReport = new byte[0];
        String category = "salary";

        given(incomeServiceMock
                .generateMonthlyExcelReport(USER_ID, MONTH, YEAR, category)).willReturn(incomesExcelReport);

        ResponseEntity<byte[]> excelData = incomeApiControllerInject.getMonthlyIncomeReport(AUTH_HEADER, MONTH, YEAR, category);

        verify(incomeServiceMock, times(1))
                .generateMonthlyExcelReport(USER_ID, MONTH, YEAR, category);

        Assertions.assertNotNull(excelData);
        Assertions.assertEquals(HttpStatus.OK, excelData.getStatusCode());
        Assertions.assertEquals(0, excelData.getBody().length);
    }

    @Test
    void getDeletedIncomesInAMonthTest(){
        List<IncomeDeletedDto> deletedIncomesList = new ArrayList<>(List.of(setIncomeDeletedDto()));

        given(incomeServiceMock
                .getDeletedIncomesInAMonth(USER_ID, MONTH, YEAR)).willReturn(deletedIncomesList);

        ResponseEntity<List<IncomeDeletedDto>> fetchedDeletedIncomesList =
                incomeApiControllerInject.getDeletedIncomesInAMonth(AUTH_HEADER, MONTH, YEAR);

        verify(incomeServiceMock, times(1))
                .getDeletedIncomesInAMonth(USER_ID, MONTH, YEAR);

        Assertions.assertNotNull(fetchedDeletedIncomesList);
        Assertions.assertEquals(HttpStatus.OK, fetchedDeletedIncomesList.getStatusCode());
        Assertions.assertEquals(1, fetchedDeletedIncomesList.getBody().size());
    }

    @Test
    void getAllIncomesByYearTest(){
        List<IncomeDetailsDto> incomesList = new ArrayList<>(List.of(setIncomeDetailsDto()));
        String category = "salary";
        boolean deleteStatus = false;

        given(incomeServiceMock
                .getAllIncomesByYear(USER_ID, MONTH, category, deleteStatus)).willReturn(incomesList);

        ResponseEntity<List<IncomeDetailsDto>> fetchedIncomesList =
                incomeApiControllerInject.getAllIncomesByYear(AUTH_HEADER, MONTH, category, deleteStatus);

        verify(incomeServiceMock, times(1))
                .getAllIncomesByYear(USER_ID, MONTH, category, deleteStatus);

        Assertions.assertNotNull(fetchedIncomesList);
        Assertions.assertEquals(HttpStatus.OK, fetchedIncomesList.getStatusCode());
        Assertions.assertEquals(1, fetchedIncomesList.getBody().size());
    }

    @Test
    void getYearlyIncomeReportTest(){
        byte[] incomesExcelReport = new byte[0];
        String category = "salary";

        given(incomeServiceMock
                .generateYearlyExcelReport(USER_ID, MONTH, category)).willReturn(incomesExcelReport);

        ResponseEntity<byte[]> excelData = incomeApiControllerInject.getYearlyIncomeReport(AUTH_HEADER, MONTH, category);

        verify(incomeServiceMock, times(1)).generateYearlyExcelReport(USER_ID, MONTH, category);

        Assertions.assertNotNull(excelData);
        Assertions.assertEquals(HttpStatus.OK, excelData.getStatusCode());
        Assertions.assertEquals(0, excelData.getBody().length);
    }

    @Test
    void getTotalIncomeByMonthAndYearTest(){
        BigDecimal totalIncome = new BigDecimal("123432");

        given(incomeServiceMock.getTotalIncomeInMonthAndYear(USER_ID, MONTH, YEAR)).willReturn(totalIncome);

        BigDecimal fetchedTotalIncome = incomeApiControllerInject.getTotalIncomeByMonthAndYear(AUTH_HEADER, MONTH, YEAR);

        verify(incomeServiceMock, times(1)).getTotalIncomeInMonthAndYear(USER_ID, MONTH, YEAR);

        Assertions.assertNotNull(fetchedTotalIncome);
        Assertions.assertEquals(totalIncome, fetchedTotalIncome);
    }

    @Test
    void getMonthlyTotalsTest(){
        List<BigDecimal> monthlyList = new ArrayList<>(List.of(new BigDecimal("2346643")));

        given(incomeServiceMock.getMonthlyIncomes(USER_ID, YEAR)).willReturn(monthlyList);

        List<BigDecimal> fetchedmonthlyList = incomeApiControllerInject.getMonthlyTotals(AUTH_HEADER, YEAR);

        verify(incomeServiceMock, times(1)).getMonthlyIncomes(USER_ID, YEAR);

        Assertions.assertNotNull(fetchedmonthlyList);
        Assertions.assertEquals(1, fetchedmonthlyList.size());
    }

    @Test
    void getRemainingIncomeUpToPreviousMonthByMonthAndYearTest(){
        BigDecimal totalIncome = new BigDecimal("123432");

        given(incomeServiceMock.getRemainingIncomeUpToPreviousMonthByMonthAndYear(USER_ID, MONTH, YEAR)).willReturn(totalIncome);

        BigDecimal fetchedTotalIncome =
                incomeApiControllerInject.getRemainingIncomeUpToPreviousMonthByMonthAndYear(AUTH_HEADER, MONTH, YEAR);

        verify(incomeServiceMock, times(1)).getRemainingIncomeUpToPreviousMonthByMonthAndYear(USER_ID, MONTH, YEAR);

        Assertions.assertNotNull(fetchedTotalIncome);
        Assertions.assertEquals(totalIncome, fetchedTotalIncome);
    }

    @Test
    void incomeUpdateCheckFunctionTest(){
        IncomeModel income = setIncomeModel();

        given(incomeServiceMock.incomeUpdateCheckFunction(income, USER_ID)).willReturn(FLAG);

        boolean fetchedFlagValue =
                incomeApiControllerInject.incomeUpdateCheckFunction(AUTH_HEADER, income);

        verify(incomeServiceMock, times(1)).incomeUpdateCheckFunction(income, USER_ID);

        Assertions.assertTrue(fetchedFlagValue);
    }

    @Test
    void incomeDeleteCheckFunctionTest(){
        IncomeModel income = setIncomeModel();

        given(incomeServiceMock.incomeDeleteCheckFunction(income)).willReturn(FLAG);

        boolean fetchedFlagValue =
                incomeApiControllerInject.incomeDeleteCheckFunction(AUTH_HEADER, income);

        verify(incomeServiceMock, times(1)).incomeDeleteCheckFunction(income);

        Assertions.assertTrue(fetchedFlagValue);
    }

    @Test
    void incomeRevertFunctionTest(){
        given(incomeServiceMock.incomeRevertFunction(INCOME_ID, USER_ID)).willReturn(FLAG);

        boolean fetchedFlagValue =
                incomeApiControllerInject.incomeRevertFunction(AUTH_HEADER, INCOME_ID);

        verify(incomeServiceMock, times(1)).incomeRevertFunction(INCOME_ID, USER_ID);

        Assertions.assertTrue(fetchedFlagValue);
    }

    @Test
    void getAvailableBalanceOfUserTest(){
        BigDecimal availableBalance = new BigDecimal("123432");

        given(incomeServiceMock.getAvailableBalanceOfUser(USER_ID)).willReturn(availableBalance);

        BigDecimal fetchedAvailableBalance = incomeApiControllerInject.getAvailableBalanceOfUser(AUTH_HEADER);

        verify(incomeServiceMock, times(1)).getAvailableBalanceOfUser(USER_ID);

        Assertions.assertNotNull(fetchedAvailableBalance);
        Assertions.assertEquals(availableBalance, fetchedAvailableBalance);
    }

    @Test
    void updateIncomeTest(){
        IncomeModel income = setIncomeModel();

        given(incomeServiceMock.updateBySource(INCOME_ID, USER_ID, income)).willReturn(null);

        ResponseEntity<IncomeDetailsDto> updatedIncome =
                incomeApiControllerInject.updateIncome(AUTH_HEADER, INCOME_ID, income);

        verify(incomeServiceMock, times(1)).updateBySource(INCOME_ID, USER_ID, income);

        Assertions.assertNull(updatedIncome);
    }

    @Test
    void deleteIncomeByIdTest(){
        boolean isDeleted = false;

        given(incomeServiceMock.deleteIncomeById(INCOME_ID, USER_ID)).willReturn(isDeleted);

        ResponseEntity<Void> output = incomeApiControllerInject.deleteIncomeById(AUTH_HEADER, INCOME_ID);

        verify(incomeServiceMock, times(1)).deleteIncomeById(INCOME_ID, USER_ID);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, output.getStatusCode());
    }

    @Test
    void deleteIncomeByIdTrueTest(){
        boolean isDeleted = true;

        given(incomeServiceMock.deleteIncomeById(INCOME_ID, USER_ID)).willReturn(isDeleted);

        ResponseEntity<Void> output = incomeApiControllerInject.deleteIncomeById(AUTH_HEADER, INCOME_ID);

        verify(incomeServiceMock, times(1)).deleteIncomeById(INCOME_ID, USER_ID);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(HttpStatus.NO_CONTENT, output.getStatusCode());
    }

    private IncomeModel setIncomeModel(){
        IncomeModel income = new IncomeModel();
        income.setUserId(1L);
        income.setAmount(new BigDecimal("2343542"));
        income.setSource("ust-salary");
        income.setDate(LocalDate.of(2025, 01, 01));
        income.setCategory("salary");
        income.setRecurring(true);
        income.setDeleted(false);
        return income;
    }

    private IncomeDetailsDto setIncomeDetailsDto(){
        IncomeDetailsDto income = new IncomeDetailsDto();
        income.setAmount(new BigDecimal("2343542"));
        income.setSource("ust-salary");
        income.setDate(Date.valueOf("2025-01-01"));
        income.setCategory("salary");
        income.setRecurring(true);
        return income;
    }

    private IncomeDeletedDto setIncomeDeletedDto(){
        IncomeDeletedDto deletedIncome = new IncomeDeletedDto();
        deletedIncome.setAmount(new BigDecimal("2343542"));
        deletedIncome.setSource("ust-salary");
        deletedIncome.setDate(LocalDate.of(2025, 01, 01));
        deletedIncome.setCategory("salary");
        deletedIncome.setRecurring(true);
        return deletedIncome;
    }
}
