package com.moneyfi.income.service.impl;

import com.moneyfi.income.model.IncomeDeleted;
import com.moneyfi.income.model.IncomeModel;
import com.moneyfi.income.repository.IncomeDeletedRepository;
import com.moneyfi.income.repository.IncomeRepository;
import com.moneyfi.income.repository.common.IncomeCommonRepository;
import com.moneyfi.income.service.dto.response.IncomeDeletedDto;
import com.moneyfi.income.service.dto.response.IncomeDetailsDto;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class IncomeServiceImplementationTest {

    @InjectMocks
    private IncomeServiceImpl incomeServiceInject;
    @Mock
    private IncomeRepository incomeRepositoryMock;
    @Mock
    private IncomeCommonRepository incomeCommonRepositoryMock;
    @Mock
    private IncomeDeletedRepository incomeDeletedRepositoryMock;

    private static final Long USER_ID = 1L;
    private static final Long INCOME_ID = 7L;
    private static final int MONTH = 5;
    private static final int YEAR = 2025;
    private static final String CATEGORY = "All";

    @BeforeEach
    void init(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveTest(){
        IncomeModel incomeModel = setIncomeModel();

        given(incomeRepositoryMock.getIncomeBySourceAndCategory(anyLong(), anyString(), anyString(), any()))
                .willReturn(null);
        given(incomeRepositoryMock.save(any())).willReturn(incomeModel);

        IncomeModel incomeModel1 = incomeServiceInject.save(incomeModel);

        verify(incomeRepositoryMock, times(1))
                .getIncomeBySourceAndCategory(incomeModel.getUserId(), incomeModel.getSource(), incomeModel.getCategory(), incomeModel.getDate());
        verify(incomeRepositoryMock, times(1)).save(incomeModel);

        Assertions.assertNotNull(incomeModel1);
        Assertions.assertEquals(incomeModel, incomeModel1);
    }

    @Test
    void saveIncomeModelNullTest(){
        IncomeModel incomeModel = setIncomeModel();

        given(incomeRepositoryMock.getIncomeBySourceAndCategory(anyLong(), anyString(), anyString(), any()))
                .willReturn(incomeModel);

        IncomeModel incomeModel1 = incomeServiceInject.save(incomeModel);

        verify(incomeRepositoryMock, times(1))
                .getIncomeBySourceAndCategory(incomeModel.getUserId(), incomeModel.getSource(), incomeModel.getCategory(), incomeModel.getDate());

        Assertions.assertNull(incomeModel1);
    }

    @Test
    void getAllIncomesTest(){
        List<IncomeModel> incomesList = new ArrayList<>();
        incomesList.add(setIncomeModel());

        given(incomeRepositoryMock.findIncomesOfUser(anyLong())).willReturn(incomesList);

        List<IncomeModel> list = incomeServiceInject.getAllIncomes(USER_ID);

        verify(incomeRepositoryMock, times(1)).findIncomesOfUser(USER_ID);

        Assertions.assertNotNull(list);
        Assertions.assertEquals(1, list.size());
    }

    @Test
    void getAllIncomesByMonthYearAndCategoryTest(){
        List<IncomeDetailsDto> incomesList = new ArrayList<>();
        incomesList.add(setIncomeDetailsDtoList());
        boolean isDeleted = false;

        given(incomeCommonRepositoryMock.getAllIncomesByDate(anyLong(), anyInt(), anyInt(), anyString(), anyBoolean()))
                .willReturn(incomesList);

        List<IncomeDetailsDto> list = incomeServiceInject.getAllIncomesByMonthYearAndCategory(USER_ID, MONTH, YEAR, CATEGORY, isDeleted);

        verify(incomeCommonRepositoryMock, times(1))
                .getAllIncomesByDate(USER_ID, MONTH, YEAR, CATEGORY, isDeleted);

        Assertions.assertNotNull(list);
        Assertions.assertEquals(1, list.size());
    }

    @Test
    void generateMonthlyExcelReportTest(){
        List<IncomeDetailsDto> incomesList = new ArrayList<>();
        incomesList.add(setIncomeDetailsDtoList());
        boolean isDeleted = false;

        given(incomeCommonRepositoryMock.getAllIncomesByDate(anyLong(), anyInt(), anyInt(), anyString(), anyBoolean()))
                .willReturn(incomesList);

        byte[] excel = incomeServiceInject.generateMonthlyExcelReport(USER_ID, MONTH, YEAR, CATEGORY);

        verify(incomeCommonRepositoryMock, times(1))
                .getAllIncomesByDate(USER_ID, MONTH, YEAR, CATEGORY, isDeleted);

        Assertions.assertNotNull(excel);
    }

    @Test
    void generateMonthlyExcelReportWithNotRecurringTest(){
        List<IncomeDetailsDto> incomesList = new ArrayList<>();
        IncomeDetailsDto incomeDetailsDto = setIncomeDetailsDtoList();
        incomeDetailsDto.setRecurring(false);
        incomesList.add(incomeDetailsDto);
        boolean isDeleted = false;

        given(incomeCommonRepositoryMock.getAllIncomesByDate(anyLong(), anyInt(), anyInt(), anyString(), anyBoolean()))
                .willReturn(incomesList);

        byte[] excel = incomeServiceInject.generateMonthlyExcelReport(USER_ID, MONTH, YEAR, CATEGORY);

        verify(incomeCommonRepositoryMock, times(1))
                .getAllIncomesByDate(USER_ID, MONTH, YEAR, CATEGORY, isDeleted);

        Assertions.assertNotNull(excel);
    }

    @Test
    void getDeletedIncomesInAMonthTest(){
        List<IncomeDeletedDto> deletedIncomesList = new ArrayList<>();

        given(incomeCommonRepositoryMock.getDeletedIncomesInAMonth(anyLong(), anyInt(), anyInt()))
                .willReturn(deletedIncomesList);

        List<IncomeDeletedDto> list = incomeServiceInject.getDeletedIncomesInAMonth(USER_ID, MONTH, YEAR);

        verify(incomeCommonRepositoryMock, times(1))
                .getDeletedIncomesInAMonth(USER_ID, MONTH, YEAR);

        Assertions.assertNotNull(list);
        Assertions.assertEquals(0, list.size());
    }

    @Test
    void getAllIncomesByYearTest(){
        List<IncomeDetailsDto> incomesList = new ArrayList<>();
        incomesList.add(setIncomeDetailsDtoList());
        boolean isDeleted = false;

        given(incomeCommonRepositoryMock.getAllIncomesByYear(anyLong(), anyInt(), anyString(), anyBoolean()))
                .willReturn(incomesList);

        List<IncomeDetailsDto> list = incomeServiceInject.getAllIncomesByYear(USER_ID, YEAR, CATEGORY, isDeleted);

        verify(incomeCommonRepositoryMock, times(1))
                .getAllIncomesByYear(USER_ID, YEAR, CATEGORY, isDeleted);

        Assertions.assertNotNull(list);
        Assertions.assertEquals(1, list.size());
    }

    @Test
    void generateYearlyExcelReportTest(){
        List<IncomeDetailsDto> incomesList = new ArrayList<>();
        IncomeDetailsDto incomeDetailsDto = setIncomeDetailsDtoList();
        incomeDetailsDto.setRecurring(false);
        incomesList.add(incomeDetailsDto);
        boolean isDeleted = false;

        given(incomeCommonRepositoryMock.getAllIncomesByYear(anyLong(), anyInt(), anyString(), anyBoolean()))
                .willReturn(incomesList);

        byte[] excel = incomeServiceInject.generateYearlyExcelReport(USER_ID, YEAR, CATEGORY);

        verify(incomeCommonRepositoryMock, times(1))
                .getAllIncomesByYear(USER_ID, YEAR, CATEGORY, isDeleted);

        Assertions.assertNotNull(excel);
    }


    @Test
    void getMonthlyIncomesTest(){
        List<Object[]> dummyList = new ArrayList<>();
        dummyList.add(new Object[]{1, new BigDecimal("1000.00")});  // January
        dummyList.add(new Object[]{2, new BigDecimal("1100.00")});  // February
        dummyList.add(new Object[]{3, new BigDecimal("1200.00")});  // March
        dummyList.add(new Object[]{4, new BigDecimal("1300.00")});  // April
        dummyList.add(new Object[]{5, new BigDecimal("1400.00")});  // May
        dummyList.add(new Object[]{6, new BigDecimal("1500.00")});  // June
        dummyList.add(new Object[]{7, new BigDecimal("1600.00")});  // July
        dummyList.add(new Object[]{8, new BigDecimal("1700.00")});  // August
        dummyList.add(new Object[]{9, new BigDecimal("1800.00")});  // September
        dummyList.add(new Object[]{10, new BigDecimal("1900.00")}); // October
        dummyList.add(new Object[]{11, new BigDecimal("2000.00")}); // November
        dummyList.add(new Object[]{12, new BigDecimal("2100.00")}); // December


        given(incomeRepositoryMock.findMonthlyIncomes(anyLong(), anyInt(), anyBoolean())).willReturn(dummyList);

        List<BigDecimal> list = incomeServiceInject.getMonthlyIncomes(USER_ID, YEAR);

        verify(incomeRepositoryMock, times(1)).findMonthlyIncomes(USER_ID, YEAR, false);

        Assertions.assertNotNull(list);
        Assertions.assertEquals(12, list.size());
    }

    @Test
    void getTotalIncomeInMonthAndYearTest(){
        BigDecimal totalIncome = new BigDecimal("12345");

        given(incomeRepositoryMock.getTotalIncomeInMonthAndYear(anyLong(), anyInt(), anyInt())).willReturn(totalIncome);

        BigDecimal totalIncome1 = incomeServiceInject.getTotalIncomeInMonthAndYear(USER_ID, MONTH, YEAR);

        verify(incomeRepositoryMock, times(1)).getTotalIncomeInMonthAndYear(USER_ID, MONTH, YEAR);

        Assertions.assertNotNull(totalIncome1);
        Assertions.assertEquals(totalIncome, totalIncome1);
    }

    @Test
    void getTotalIncomeInMonthAndYearIncomeNullTest(){

        given(incomeRepositoryMock.getTotalIncomeInMonthAndYear(anyLong(), anyInt(), anyInt())).willReturn(null);

        BigDecimal totalIncome1 = incomeServiceInject.getTotalIncomeInMonthAndYear(USER_ID, MONTH, YEAR);

        verify(incomeRepositoryMock, times(1)).getTotalIncomeInMonthAndYear(USER_ID, MONTH, YEAR);

        Assertions.assertNotNull(totalIncome1);
        Assertions.assertEquals(BigDecimal.ZERO, totalIncome1);
    }

//    @Test
//    public void getRemainingIncomeUpToPreviousMonthByMonthAndYearTest(){
//        BigDecimal totalIncome = new BigDecimal("12345");
//        BigDecimal totalExpense = new BigDecimal("12345");
//        int month = 5;
//        int year = 2025;
//
//        given(incomeCommonRepositoryMock.getRemainingIncomeUpToPreviousMonthByMonthAndYear(
//                anyLong(), anyInt(), anyInt())).willReturn(totalIncome);
//
//        given(restTemplateMock.getForObject(anyString(), eq(BigDecimal.class))).willReturn(totalExpense);
//
//        BigDecimal totalRemIncome = incomeServiceImplementationInject.getRemainingIncomeUpToPreviousMonthByMonthAndYear(userId, month, year);
//
//        verify(incomeCommonRepositoryMock, times(1))
//                .getRemainingIncomeUpToPreviousMonthByMonthAndYear(userId, month, year);
//        verify(restTemplateMock, times(1)).getForObject(anyString(), eq(BigDecimal.class));
//
//        Assertions.assertNotNull(totalRemIncome);
//        Assertions.assertEquals(BigDecimal.ZERO, totalRemIncome);
//    }
//
//    @Test
//    public void getRemainingIncomeUpToPreviousMonthByMonthAndYearIncomeNullTest(){
//        int month = 1;
//        int year = 2025;
//
//        given(incomeRepositoryMock.getRemainingIncomeUpToPreviousMonthByMonthAndYear(
//                anyLong(), anyInt(), anyInt())).willReturn(null);
//
//        BigDecimal totalIncome = incomeServiceImplementationInject.getRemainingIncomeUpToPreviousMonthByMonthAndYear(userId, month, year);
//
//        verify(incomeRepositoryMock, times(1))
//                .getRemainingIncomeUpToPreviousMonthByMonthAndYear(userId, month+12, year-1);
//
//        Assertions.assertNotNull(totalIncome);
//        Assertions.assertEquals(BigDecimal.ZERO, totalIncome);
//    }
//
//    @Test
//    public void getRemainingIncomeUpToPreviousMonthByMonthAndYearIncomeZeroTest(){
//        int month = 1;
//        int year = 2025;
//
//        given(incomeRepositoryMock.getRemainingIncomeUpToPreviousMonthByMonthAndYear(
//                anyLong(), anyInt(), anyInt())).willReturn(BigDecimal.ZERO);
//
//        BigDecimal totalIncome = incomeServiceImplementationInject.getRemainingIncomeUpToPreviousMonthByMonthAndYear(userId, month, year);
//
//        verify(incomeRepositoryMock, times(1))
//                .getRemainingIncomeUpToPreviousMonthByMonthAndYear(userId, month+12, year-1);
//
//        Assertions.assertNotNull(totalIncome);
//        Assertions.assertEquals(BigDecimal.ZERO, totalIncome);
//    }
//
//    @Test
//    public void getRemainingIncomeUpToPreviousMonthByMonthAndYearWithExpenseGreaterThanIncomeTest(){
//        BigDecimal totalIncome = new BigDecimal("12345");
//        BigDecimal totalExpense = new BigDecimal("123456");
//        int month = 1;
//        int year = 2025;
//
//        given(incomeRepositoryMock.getRemainingIncomeUpToPreviousMonthByMonthAndYear(
//                anyLong(), anyInt(), anyInt())).willReturn(totalIncome);
//        given(restTemplateMock.getForObject(anyString(), eq(BigDecimal.class))).willReturn(totalExpense);
//
//        BigDecimal totalRemIncome = incomeServiceImplementationInject
//                .getRemainingIncomeUpToPreviousMonthByMonthAndYear(userId, month, year);
//
//        verify(incomeRepositoryMock, times(1))
//                .getRemainingIncomeUpToPreviousMonthByMonthAndYear(userId, month+12, year-1);
//        verify(restTemplateMock, times(1)).getForObject(anyString(), eq(BigDecimal.class));
//
//        Assertions.assertNotNull(totalRemIncome);
//        Assertions.assertEquals(BigDecimal.ZERO, totalRemIncome);
//    }

    @Test
    void incomeRevertFunctionTest(){
        IncomeDeleted incomeDeleted = setIncomeDeleted();
        IncomeModel income = setIncomeModel();

        given(incomeDeletedRepositoryMock.findByIncomeId(anyLong())).willReturn(incomeDeleted);
        given(incomeRepositoryMock.findById(anyLong())).willReturn(Optional.of(income));
        given(incomeRepositoryMock.save(any())).willReturn(income);
        doNothing().when(incomeDeletedRepositoryMock).deleteByIncomeId(anyLong());

        boolean flag = incomeServiceInject.incomeRevertFunction(INCOME_ID, USER_ID);

        verify(incomeDeletedRepositoryMock, times(1)).findByIncomeId(anyLong());
        verify(incomeRepositoryMock, times(1)).findById(anyLong());
        verify(incomeRepositoryMock, times(1)).save(any());
        verify(incomeDeletedRepositoryMock, times(1)).deleteByIncomeId(anyLong());

        Assertions.assertTrue(flag);
    }

    @Test
    void incomeRevertFunctionUserIdConditionMismatchTest(){
        IncomeDeleted incomeDeleted = setIncomeDeleted();
        IncomeModel income = setIncomeModel();

        given(incomeDeletedRepositoryMock.findByIncomeId(anyLong())).willReturn(incomeDeleted);
        given(incomeRepositoryMock.findById(anyLong())).willReturn(Optional.of(income));

        boolean flag = incomeServiceInject.incomeRevertFunction(INCOME_ID, 2L);

        verify(incomeDeletedRepositoryMock, times(1)).findByIncomeId(anyLong());
        verify(incomeRepositoryMock, times(1)).findById(anyLong());

        Assertions.assertFalse(flag);
    }

    @Test
    void incomeRevertFunctionIncomeNullTest(){
        IncomeDeleted incomeDeleted = setIncomeDeleted();

        given(incomeDeletedRepositoryMock.findByIncomeId(anyLong())).willReturn(incomeDeleted);
        given(incomeRepositoryMock.findById(anyLong())).willReturn(Optional.empty());

        boolean flag = incomeServiceInject.incomeRevertFunction(INCOME_ID, 2L);

        verify(incomeDeletedRepositoryMock, times(1)).findByIncomeId(anyLong());
        verify(incomeRepositoryMock, times(1)).findById(anyLong());

        Assertions.assertFalse(flag);
    }

    @Test
    void incomeRevertFunctionNumberOfDaysZeroTest(){
        IncomeDeleted incomeDeleted = setIncomeDeleted();
        incomeDeleted.setExpiryDateTime(LocalDateTime.now());

        given(incomeDeletedRepositoryMock.findByIncomeId(anyLong())).willReturn(incomeDeleted);

        boolean flag = incomeServiceInject.incomeRevertFunction(INCOME_ID, 2L);

        verify(incomeDeletedRepositoryMock, times(1)).findByIncomeId(anyLong());

        Assertions.assertFalse(flag);
    }

    @Test
    void updateBySourceTest(){
        IncomeModel updatedIncome = setIncomeModel();
        updatedIncome.setSource("Business");

        given(incomeRepositoryMock.findById(anyLong())).willReturn(Optional.of(setIncomeModel()));
        given(incomeRepositoryMock.save(any())).willReturn(updatedIncome);

        ResponseEntity<IncomeDetailsDto> fetchedUpdatedIncome = incomeServiceInject.updateBySource(anyLong(), USER_ID, updatedIncome);

        verify(incomeRepositoryMock, times(1)).findById(anyLong());
        verify(incomeRepositoryMock, times(1)).save(any());

        Assertions.assertNotNull(fetchedUpdatedIncome);
        Assertions.assertEquals(HttpStatus.CREATED, fetchedUpdatedIncome.getStatusCode());
    }

    @Test
    void updateBySourceNoContentTest(){
        IncomeModel income = setIncomeModel();

        given(incomeRepositoryMock.findById(anyLong())).willReturn(Optional.of(income));

        ResponseEntity<IncomeDetailsDto> fetchedUpdatedIncome = incomeServiceInject.updateBySource(anyLong(), USER_ID, income);

        verify(incomeRepositoryMock, times(1)).findById(anyLong());

        Assertions.assertNotNull(fetchedUpdatedIncome);
        Assertions.assertEquals(HttpStatus.NO_CONTENT, fetchedUpdatedIncome.getStatusCode());
    }

    @Test
    void updateBySourceIncomeModelNullTest(){
        IncomeModel income = setIncomeModel();

        given(incomeRepositoryMock.findById(anyLong())).willReturn(Optional.empty());

        ResponseEntity<IncomeDetailsDto> fetchedUpdatedIncome = incomeServiceInject.updateBySource(anyLong(), USER_ID, income);

        verify(incomeRepositoryMock, times(1)).findById(anyLong());

        Assertions.assertNotNull(fetchedUpdatedIncome);
        Assertions.assertEquals(HttpStatus.CONFLICT, fetchedUpdatedIncome.getStatusCode());
    }

    @Test
    void deleteIncomeByIdTest(){
        IncomeModel income = setIncomeModel();

        given(incomeRepositoryMock.findById(anyLong())).willReturn(Optional.of(income));
        given(incomeDeletedRepositoryMock.save(any())).willReturn(new IncomeDeleted());

        boolean flag = incomeServiceInject.deleteIncomeById(INCOME_ID, USER_ID);

        verify(incomeRepositoryMock, times(1)).findById(anyLong());
        verify(incomeDeletedRepositoryMock, times(1)).save(any());

        Assertions.assertTrue(flag);
    }

    @Test
    void deleteIncomeByIdFalseTest(){
        given(incomeRepositoryMock.findById(anyLong())).willReturn(Optional.empty());

        boolean flag = incomeServiceInject.deleteIncomeById(INCOME_ID, USER_ID);

        verify(incomeRepositoryMock, times(1)).findById(anyLong());

        Assertions.assertFalse(flag);
    }

    @Test
    void getAvailableBalanceOfUserTest(){
        BigDecimal availableBalance = new BigDecimal("2343433");

        given(incomeRepositoryMock.getAvailableBalanceOfUser(USER_ID)).willReturn(availableBalance);

        BigDecimal fetchedAvailableBalance = incomeServiceInject.getAvailableBalanceOfUser(USER_ID);

        verify(incomeRepositoryMock, times(1)).getAvailableBalanceOfUser(USER_ID);

        Assertions.assertNotNull(fetchedAvailableBalance);
        Assertions.assertEquals(availableBalance, fetchedAvailableBalance);
    }

    private IncomeModel setIncomeModel(){
        IncomeModel incomeModel = new IncomeModel();
        LocalDate date = LocalDate.of(2025, 4, 4);

        incomeModel.setUserId(1L);
        incomeModel.setSource("ust-salary");
        incomeModel.setCategory("salary");
        incomeModel.setAmount(new BigDecimal("58400"));
        incomeModel.setDate(date);
        incomeModel.setRecurring(true);
        incomeModel.setDeleted(false);
        return incomeModel;
    }

    private IncomeDetailsDto setIncomeDetailsDtoList(){
        IncomeDetailsDto incomeDetailsDto = new IncomeDetailsDto();

        incomeDetailsDto.setSource("ust-salary");
        incomeDetailsDto.setCategory("salary");
        incomeDetailsDto.setAmount(new BigDecimal("58400"));
        incomeDetailsDto.setDate(Date.valueOf("2025-01-01"));
        incomeDetailsDto.setRecurring(true);
        return incomeDetailsDto;
    }

    private IncomeDeleted setIncomeDeleted(){
        IncomeDeleted incomeDeleted = new IncomeDeleted();
        incomeDeleted.setId(2L);
        incomeDeleted.setIncomeId(10L);
        incomeDeleted.setStartDateTime(LocalDateTime.of(2024, 01, 01, 12, 30, 12));
        incomeDeleted.setExpiryDateTime(LocalDateTime.now().plusDays(10));
        return incomeDeleted;
    }
}
