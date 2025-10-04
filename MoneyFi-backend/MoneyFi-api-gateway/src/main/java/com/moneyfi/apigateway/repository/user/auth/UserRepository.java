package com.moneyfi.apigateway.repository.user.auth;

import com.moneyfi.apigateway.model.auth.UserAuthModel;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface UserRepository extends JpaRepository<UserAuthModel, Long> {

    @Query(nativeQuery = true, value = "exec getUserAuthDetailsListWhoseOtpCountGreaterThanThree @startOfToday = :startOfToday")
    List<UserAuthModel> getUserListWhoseOtpCountGreaterThanThree(LocalDateTime startOfToday);

    @Query(nativeQuery = true, value = "exec getUserAuthDetailsByUsername @username = :email")
    UserAuthModel getUserDetailsByUsername(String email);

    @Query(nativeQuery = true, value = "exec updateRecurringIncomesAndExpenses")
    @Transactional
    @Modifying
    void updateRecurringIncomesAndExpenses();

    @Query(value = """
            SELECT uat.*
            FROM user_auth_table uat WITH (NOLOCK)
            INNER JOIN user_auth_hist_table uaht WITH (NOLOCK) ON uaht.user_id = uat.id
            WHERE uat.is_deleted = 1
            	AND uaht.reason_type_id = :reasonTypeId
            	AND uat.role_id = :roleId
            	AND uaht.updated_time < DATEADD(DAY, -30, GETDATE())
            """, nativeQuery = true)
    List<UserAuthModel> getDeletedUsersList(int roleId, int reasonTypeId);

    @Modifying
    @Transactional
    @Query(value = """
            DELETE FROM income_table_deleted WHERE income_id IN (SELECT id FROM income_table WHERE user_id IN (:list));
            DELETE FROM income_table WHERE user_id IN (:list);
            DELETE FROM expense_table WHERE user_id IN (:list);
            DELETE FROM budget_table WHERE user_id IN (:list);
            DELETE FROM goal_table WHERE user_id IN (:list);
            """, nativeQuery = true)
    void deleteIncomeExpenseBudgetGoalsOfDeletedUsers(List<Long> list);
}
