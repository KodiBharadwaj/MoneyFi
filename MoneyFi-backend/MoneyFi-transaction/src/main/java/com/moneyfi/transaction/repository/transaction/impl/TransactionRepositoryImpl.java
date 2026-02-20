package com.moneyfi.transaction.repository.transaction.impl;

import com.microsoft.sqlserver.jdbc.SQLServerDataTable;
import com.moneyfi.transaction.exceptions.QueryValidationException;
import com.moneyfi.transaction.repository.transaction.TransactionRepository;
import com.moneyfi.transaction.service.expense.dto.response.ExpenseDetailsDto;
import com.moneyfi.transaction.service.income.dto.request.AccountStatementRequestDto;
import com.moneyfi.transaction.service.income.dto.request.TransactionsListRequestDto;
import com.moneyfi.transaction.service.income.dto.response.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.CallableStatement;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import static com.moneyfi.transaction.utils.StringConstants.*;
import static com.moneyfi.transaction.utils.StringConstants.YEAR;

@Repository
public class TransactionRepositoryImpl implements TransactionRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String NO_MONTHLY_INCOME_DATE = "Error occurred while fetching income data";
    private static final String NO_DELETED_INCOME_DATE = "Error occurred while fetching deleted income data";

    @Override
    public List<IncomeDetailsDto> getAllIncomesByDate(Long userId, TransactionsListRequestDto requestDto) {
        try {
            List<IncomeDetailsDto> incomesList = new ArrayList<>();
            Query query = entityManager.createNativeQuery(
                            "exec [getAllIncomesByUserRequest] " +
                                    "@userId = :userId, " +
                                    "@date = :date, " +
                                    "@deleteStatus = :deleteStatus, " +
                                    "@requestType = :requestType, " +
                                    "@category = :category, " +
                                    "@offset = :offset, " +
                                    "@limit = :limit ")
                    .setParameter(USER_ID, userId)
                    .setParameter(DATE, requestDto.getDate())
                    .setParameter(DELETE_STATUS, requestDto.isDeleteStatus())
                    .setParameter(REQUEST_TYPE, requestDto.getRequestType())
                    .setParameter(CATEGORY, requestDto.getCategory())
                    .setParameter(OFFSET, requestDto.getStartIndex())
                    .setParameter(LIMIT, requestDto.getThreshold())
                    .unwrap(NativeQuery.class)
                    .setResultTransformer(Transformers.aliasToBean(IncomeDetailsDto.class));
            incomesList.addAll(query.getResultList());
            return incomesList;
        } catch (Exception e) {
            e.printStackTrace();
            throw new QueryValidationException(NO_MONTHLY_INCOME_DATE);
        }
    }

    @Override
    public List<IncomeDeletedDto> getDeletedIncomesInAMonth(Long userId, int month, int year) {
        try {
            List<IncomeDeletedDto> incomeListDeleted = new ArrayList<>();
            Query query = entityManager.createNativeQuery(
                            "exec [getDeletedIncomesInAMonth] " +
                                    "@userId = :userId, " +
                                    "@month = :month, " +
                                    "@year = :year")
                    .setParameter(USER_ID, userId)
                    .setParameter(MONTH, month)
                    .setParameter(YEAR, year)
                    .unwrap(NativeQuery.class)
                    .setResultListTransformer(Transformers.aliasToBean(IncomeDeletedDto.class));
            incomeListDeleted.addAll(query.getResultList());
            return incomeListDeleted;
        } catch (Exception e) {
            e.printStackTrace();
            throw new QueryValidationException(NO_DELETED_INCOME_DATE);
        }
    }

    /**
    @Override
    public List<AccountStatementResponseDto> getAccountStatementOfUser(Long userId, AccountStatementRequestDto inputDto) {
        Date startDate = Date.valueOf(inputDto.getFromDate());
        Date endDate = Date.valueOf(inputDto.getToDate());

        List<AccountStatementResponseDto> accountStatement = new ArrayList<>();
        try {
            Query query = entityManager.createNativeQuery(
                            "exec getAccountStatementOfUser " +
                                    "@userId = :userId, " +
                                    "@startDate = :startDate, " +
                                    "@endDate = :endDate, " +
                                    "@offset = :offset, " +
                                    "@limit = :limit ")
                    .setParameter(USER_ID, userId)
                    .setParameter(START_DATE, startDate)
                    .setParameter(END_DATE, endDate)
                    .setParameter(OFFSET, inputDto.getStartIndex())
                    .setParameter(LIMIT, inputDto.getThreshold())
                    .unwrap(NativeQuery.class)
                    .setResultTransformer(Transformers.aliasToBean(AccountStatementResponseDto.class));
            accountStatement.addAll(query.getResultList());
            return accountStatement;
        } catch (Exception e) {
            e.printStackTrace();
            throw new QueryValidationException("Error occurred while fetching user's account statement");
        }
    }
    **/
    @Override
    public List<AccountStatementResponseDto> getAccountStatementOfUser(Long userId, AccountStatementRequestDto inputDto) {
        Date startDate = Date.valueOf(inputDto.getFromDate());
        Date endDate = Date.valueOf(inputDto.getToDate());
        try {
            String sql = "EXEC getAccountStatementOfUser @userId = ?, @startDate = ?, @endDate = ?, @offset = ?, @limit = ?";
            return jdbcTemplate.query(
                    sql,
                    new Object[]{userId, startDate, endDate, inputDto.getStartIndex(), inputDto.getThreshold()},
                    (rs, rowNum) -> {
                        AccountStatementResponseDto responseDto = new AccountStatementResponseDto();
                        responseDto.setTransactionDate(rs.getDate("transactionDate"));
                        responseDto.setTransactionTime(rs.getString("transactionTime"));
                        responseDto.setDescription(rs.getString("description"));
                        responseDto.setAmount(rs.getBigDecimal("amount"));
                        responseDto.setCreditOrDebit(rs.getString("creditOrDebit"));
                        return responseDto;
                    }
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new QueryValidationException("Error occurred while fetching user's account statement");
        }
    }

    /**
    @Override
    public UserDetailsForStatementDto getUserDetailsForAccountStatement(Long userId) {
        try {
            Query query = entityManager.createNativeQuery(
                            "exec getUserDetailsForPdfGeneration " +
                                    "@userId = :userId ")
                    .setParameter(USER_ID, userId)
                    .unwrap(NativeQuery.class)
                    .setResultTransformer(Transformers.aliasToBean(UserDetailsForStatementDto.class));

            return (UserDetailsForStatementDto) query.getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            throw new QueryValidationException("Error occurred while fetching user's details");
        }
    }
     **/
    @Override
    public UserDetailsForStatementDto getUserDetailsForAccountStatement(Long userId) {
        try {
            String sql = "EXEC getUserDetailsForPdfGeneration @userId = ?";
            return jdbcTemplate.queryForObject(
                    sql,
                    new Object[]{userId},
                    (rs, rowNum) -> {
                        UserDetailsForStatementDto responseDto = new UserDetailsForStatementDto();
                        responseDto.setName(rs.getString("name"));
                        responseDto.setUsername(rs.getString("username"));
                        responseDto.setPhoneNumber(rs.getString("phoneNumber"));
                        responseDto.setAddress(rs.getString("address"));
                        return responseDto;
                    }
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new QueryValidationException("Error occurred while fetching user's details");
        }
    }

    /**
    @Override
    public OverviewPageDetailsDto getOverviewPageTileDetails(Long userId, int month, int year) {
        try {
            Query query = entityManager.createNativeQuery(
                            "exec getOverviewPageDetails " +
                                    "@userId = :userId, " +
                                    "@month = :month, " +
                                    "@year = :year ")
                    .setParameter(USER_ID, userId)
                    .setParameter(MONTH, month)
                    .setParameter(YEAR, year)
                    .unwrap(NativeQuery.class)
                    .setResultTransformer(Transformers.aliasToBean(OverviewPageDetailsDto.class));

            return (OverviewPageDetailsDto) query.getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            throw new QueryValidationException("Error occurred while fetching user's overview page details");
        }
    }
     **/
    @Override
    public OverviewPageDetailsDto getOverviewPageTileDetails(Long userId, int month, int year) {
        try {
            String sql = "EXEC getOverviewPageDetails @userId = ?, @month = ?, @year = ?";
            return jdbcTemplate.queryForObject(
                    sql,
                    new Object[]{ userId, month, year },
                    (rs, rowNum) -> {
                        OverviewPageDetailsDto dto = new OverviewPageDetailsDto();
                        dto.setAvailableBalance(rs.getBigDecimal("availableBalance"));
                        dto.setTotalExpense(rs.getBigDecimal("totalExpense"));
                        dto.setTotalBudget(rs.getBigDecimal("totalBudget"));
                        dto.setBudgetProgress(rs.getBigDecimal("budgetProgress"));
                        dto.setTotalGoalIncome(rs.getBigDecimal("totalGoalIncome"));
                        dto.setGoalProgress(rs.getBigDecimal("goalProgress"));
                        return dto;
                    }
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new QueryValidationException("Error occurred while fetching user's overview page details");
        }
    }

    @Override
    public List<ExpenseDetailsDto> getAllExpensesByDate(Long userId, TransactionsListRequestDto requestDto) {
        try {
            List<ExpenseDetailsDto> expenseList = new ArrayList<>();
            Query query = entityManager.createNativeQuery(
                            "exec [getAllExpensesByUserRequest] " +
                                    "@userId = :userId, " +
                                    "@date = :date, " +
                                    "@deleteStatus = :deleteStatus, " +
                                    "@requestType = :requestType, " +
                                    "@category = :category, " +
                                    "@offset = :offset, " +
                                    "@limit = :limit ")
                    .setParameter(USER_ID, userId)
                    .setParameter(DATE, requestDto.getDate())
                    .setParameter(DELETE_STATUS, requestDto.isDeleteStatus())
                    .setParameter(REQUEST_TYPE, requestDto.getRequestType())
                    .setParameter(CATEGORY, requestDto.getCategory())
                    .setParameter(OFFSET, requestDto.getStartIndex())
                    .setParameter(LIMIT, requestDto.getThreshold())
                    .unwrap(NativeQuery.class)
                    .setResultTransformer(Transformers.aliasToBean(ExpenseDetailsDto.class));
            expenseList.addAll(query.getResultList());
            return expenseList;
        } catch (Exception e) {
            e.printStackTrace();
            throw new QueryValidationException("Error occurred while fetching expense data");
        }
    }

    /**
    @Override
    public List<Integer> getCategoryIdsBasedOnTransactionType(String categoryType) {
        try {
            List<Integer> incomeCategoryIdList = new ArrayList<>();
            Query query = entityManager.createNativeQuery(
                            "exec [getCategoryIdsByCategoryType] " +
                                    "@categoryType = :categoryType ")
                    .setParameter("categoryType", categoryType);
            incomeCategoryIdList.addAll(query.getResultList());
            return incomeCategoryIdList;
        } catch (Exception e) {
            e.printStackTrace();
            throw new QueryValidationException("Income Category Ids not found");
        }
    }
     **/
    @Override
    public List<Integer> getCategoryIdsBasedOnTransactionType(String categoryType) {
        try {
            String sql = "EXEC getCategoryIdsByCategoryType @categoryType = ?";
            return jdbcTemplate.query(
                    sql,
                    new Object[]{ categoryType },
                    (rs, rowNum) -> rs.getInt(1)
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new QueryValidationException("Income Category Ids not found");
        }
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGmailProcessedAsVerified(List<Long> gmailProsessedIdList) {
        entityManager.unwrap(Session.class).doWork(connection -> {
            SQLServerDataTable table = new SQLServerDataTable();
            table.addColumnMetadata("id", java.sql.Types.BIGINT);
            for (Long id : gmailProsessedIdList) {
                table.addRow(id);
            }
            try (CallableStatement cs = connection.prepareCall(
                    "{call dbo.updateGmailProcessedAsVerified(?)}")) {
                cs.setObject(1, table);
                cs.execute();
            }
        });
    }
}
