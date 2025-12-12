//package com.moneyfi.income.service;
//
//import com.moneyfi.income.model.income.IncomeModel;
//import com.moneyfi.income.repository.income.IncomeRepository;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.web.client.RestTemplate;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//
//public class IncomeServiceImplementationTest {
//
//    @InjectMocks
//    private IncomeServiceImplementation incomeServiceImplementationInject;
//    @Mock
//    private IncomeRepository incomeRepositoryMock;
//    @Mock
//    private RestTemplate restTemplateMock;
//
//    @BeforeEach
//    public void init(){
//        MockitoAnnotations.initMocks(this);
//    }
//
//    private final Long userId = 1L;
//
//
//    @Test
//    public void saveTest(){
//        IncomeModel incomeModel = setIncomeModel();
//
//        given(incomeRepositoryMock.getIncomeBySourceAndCategory(anyLong(), anyString(), anyString(), any()))
//                .willReturn(null);
//        given(incomeRepositoryMock.save(any())).willReturn(incomeModel);
//
//        IncomeModel incomeModel1 = incomeServiceImplementationInject.save(incomeModel);
//
//        verify(incomeRepositoryMock, times(1))
//                .getIncomeBySourceAndCategory(incomeModel.getUserId(), incomeModel.getSource(), incomeModel.getCategory(), incomeModel.getDate());
//        verify(incomeRepositoryMock, times(1)).save(incomeModel);
//
//        Assertions.assertNotNull(incomeModel1);
//        Assertions.assertEquals(incomeModel, incomeModel1);
//    }
//
//    @Test
//    public void saveIncomeModelNullTest(){
//        IncomeModel incomeModel = setIncomeModel();
//
//        given(incomeRepositoryMock.getIncomeBySourceAndCategory(anyLong(), anyString(), anyString(), any()))
//                .willReturn(incomeModel);
//
//        IncomeModel incomeModel1 = incomeServiceImplementationInject.save(incomeModel);
//
//        verify(incomeRepositoryMock, times(1))
//                .getIncomeBySourceAndCategory(incomeModel.getUserId(), incomeModel.getSource(), incomeModel.getCategory(), incomeModel.getDate());
//
//        Assertions.assertNull(incomeModel1);
//    }
//
//    @Test
//    public void getAllIncomesTest(){
//        List<IncomeModel> incomesList = new ArrayList<>();
//        incomesList.add(setIncomeModel());
//
//        given(incomeRepositoryMock.findIncomesOfUser(anyLong())).willReturn(incomesList);
//
//        List<IncomeModel> list = incomeServiceImplementationInject.getAllIncomes(userId);
//
//        verify(incomeRepositoryMock, times(1)).findIncomesOfUser(userId);
//
//        Assertions.assertNotNull(list);
//        Assertions.assertEquals(1, list.size());
//    }
//
//    @Test
//    public void getAllIncomesByMonthYearAndCategoryTest(){
//        List<IncomeModel> incomesList = new ArrayList<>();
//        incomesList.add(setIncomeModel());
//        int month = 4;
//        int year = 2025;
//        boolean is_deleted = false;
//        String category = "all";
//
//        given(incomeRepositoryMock.getAllIncomesByDate(anyLong(), anyInt(), anyInt(), anyBoolean()))
//                .willReturn(incomesList);
//
//        List<IncomeModel> list = incomeServiceImplementationInject.getAllIncomesByMonthYearAndCategory(userId, month, year, category, is_deleted);
//
//        verify(incomeRepositoryMock, times(1))
//                .getAllIncomesByDate(userId, month, year, is_deleted);
//
//        Assertions.assertNotNull(list);
//        Assertions.assertEquals(1, list.size());
//    }
//
//    @Test
//    public void getAllIncomesByMonthYearAndCategorySalaryTest(){
//        List<IncomeModel> incomesList = new ArrayList<>();
//        incomesList.add(setIncomeModel());
//        int month = 4;
//        int year = 2025;
//        boolean is_deleted = false;
//        String category = "salary";
//
//        given(incomeRepositoryMock.getAllIncomesByDate(anyLong(), anyInt(), anyInt(), anyBoolean()))
//                .willReturn(incomesList);
//
//        List<IncomeModel> list = incomeServiceImplementationInject.getAllIncomesByMonthYearAndCategory(userId, month, year, category, is_deleted);
//
//        verify(incomeRepositoryMock, times(1))
//                .getAllIncomesByDate(userId, month, year, is_deleted);
//
//        Assertions.assertNotNull(list);
//        Assertions.assertEquals(1, list.size());
//    }
//
//    @Test
//    public void getAllIncomesByYearTest(){
//        List<IncomeModel> incomesList = new ArrayList<>();
//        incomesList.add(setIncomeModel());
//        int year = 2025;
//        boolean is_deleted = false;
//        String category = "all";
//
//        given(incomeRepositoryMock.getAllIncomesByYear(anyLong(), anyInt(), anyBoolean()))
//                .willReturn(incomesList);
//
//        List<IncomeModel> list = incomeServiceImplementationInject.getAllIncomesByYear(userId, year, category, is_deleted);
//
//        verify(incomeRepositoryMock, times(1))
//                .getAllIncomesByYear(userId, year, is_deleted);
//
//        Assertions.assertNotNull(list);
//        Assertions.assertEquals(1, list.size());
//    }
//
//    @Test
//    public void getAllIncomesByYearAndCategorySalaryTest(){
//        List<IncomeModel> incomesList = new ArrayList<>();
//        incomesList.add(setIncomeModel());
//        int month = 4;
//        int year = 2025;
//        boolean is_deleted = false;
//        String category = "salary";
//
//        given(incomeRepositoryMock.getAllIncomesByYear(anyLong(), anyInt(), anyBoolean()))
//                .willReturn(incomesList);
//
//        List<IncomeModel> list = incomeServiceImplementationInject.getAllIncomesByYear(userId, year, category, is_deleted);
//
//        verify(incomeRepositoryMock, times(1))
//                .getAllIncomesByYear(userId, year, is_deleted);
//
//        Assertions.assertNotNull(list);
//        Assertions.assertEquals(1, list.size());
//    }
//
//    @Test
//    public void getMonthlyIncomesTest(){
//        List<Object[]> dummyList = new ArrayList<>();
//        dummyList.add(new Object[]{1, new BigDecimal("1000.00")});  // January
//        dummyList.add(new Object[]{2, new BigDecimal("1100.00")});  // February
//        dummyList.add(new Object[]{3, new BigDecimal("1200.00")});  // March
//        dummyList.add(new Object[]{4, new BigDecimal("1300.00")});  // April
//        dummyList.add(new Object[]{5, new BigDecimal("1400.00")});  // May
//        dummyList.add(new Object[]{6, new BigDecimal("1500.00")});  // June
//        dummyList.add(new Object[]{7, new BigDecimal("1600.00")});  // July
//        dummyList.add(new Object[]{8, new BigDecimal("1700.00")});  // August
//        dummyList.add(new Object[]{9, new BigDecimal("1800.00")});  // September
//        dummyList.add(new Object[]{10, new BigDecimal("1900.00")}); // October
//        dummyList.add(new Object[]{11, new BigDecimal("2000.00")}); // November
//        dummyList.add(new Object[]{12, new BigDecimal("2100.00")}); // December
//
//        int year = 2025;
//
//        given(incomeRepositoryMock.findMonthlyIncomes(anyLong(), anyInt(), anyBoolean())).willReturn(dummyList);
//
//        List<BigDecimal> list = incomeServiceImplementationInject.getMonthlyIncomes(userId, year);
//
//        verify(incomeRepositoryMock, times(1)).findMonthlyIncomes(userId, year, false);
//
//        Assertions.assertNotNull(list);
//        Assertions.assertEquals(12, list.size());
//    }
//
//    @Test
//    public void getTotalIncomeInMonthAndYearTest(){
//        BigDecimal totalIncome = new BigDecimal("12345");
//        int month = 5;
//        int year = 2025;
//
//        given(incomeRepositoryMock.getTotalIncomeInMonthAndYear(anyLong(), anyInt(), anyInt())).willReturn(totalIncome);
//
//        BigDecimal totalIncome1 = incomeServiceImplementationInject.getTotalIncomeInMonthAndYear(userId, month, year);
//
//        verify(incomeRepositoryMock, times(1)).getTotalIncomeInMonthAndYear(userId, month, year);
//
//        Assertions.assertNotNull(totalIncome1);
//        Assertions.assertEquals(totalIncome, totalIncome1);
//    }
//
//    @Test
//    public void getTotalIncomeInMonthAndYearIncomeNullTest(){
//        int month = 5;
//        int year = 2025;
//
//        given(incomeRepositoryMock.getTotalIncomeInMonthAndYear(anyLong(), anyInt(), anyInt())).willReturn(null);
//
//        BigDecimal totalIncome1 = incomeServiceImplementationInject.getTotalIncomeInMonthAndYear(userId, month, year);
//
//        verify(incomeRepositoryMock, times(1)).getTotalIncomeInMonthAndYear(userId, month, year);
//
//        Assertions.assertNotNull(totalIncome1);
//        Assertions.assertEquals(BigDecimal.ZERO, totalIncome1);
//    }
//
//    @Test
//    public void getRemainingIncomeUpToPreviousMonthByMonthAndYearTest(){
//        BigDecimal totalIncome = new BigDecimal("12345");
//        BigDecimal totalExpense = new BigDecimal("12345");
//        int month = 5;
//        int year = 2025;
//
//        given(incomeRepositoryMock.getRemainingIncomeUpToPreviousMonthByMonthAndYear(
//                anyLong(), anyInt(), anyInt())).willReturn(totalIncome);
//
//        given(restTemplateMock.getForObject(anyString(), eq(BigDecimal.class))).willReturn(totalExpense);
//
//        BigDecimal totalRemIncome = incomeServiceImplementationInject.getRemainingIncomeUpToPreviousMonthByMonthAndYear(userId, month, year);
//
//        verify(incomeRepositoryMock, times(1))
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
//
//
//    public IncomeModel setIncomeModel(){
//        IncomeModel incomeModel = new IncomeModel();
//        LocalDate date = LocalDate.of(2025, 4, 4);
//
//        incomeModel.setUserId(1L);;
//        incomeModel.setSource("ust-salary");
//        incomeModel.setCategory("salary");
//        incomeModel.setAmount(new BigDecimal("58400"));
//        incomeModel.setDate(date);
//        incomeModel.setRecurring(true);
//        incomeModel.set_deleted(false);
//        return incomeModel;
//    }
//}
