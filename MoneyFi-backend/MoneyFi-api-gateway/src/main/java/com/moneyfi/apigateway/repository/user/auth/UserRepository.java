package com.moneyfi.apigateway.repository.user.auth;

import com.moneyfi.apigateway.dto.interfaces.ContactUsHistProjection;
import com.moneyfi.apigateway.dto.interfaces.ContactUsProjection;
import com.moneyfi.apigateway.dto.interfaces.ProfileDetailsProjection;
import com.moneyfi.apigateway.model.auth.UserAuthModel;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserAuthModel, Long> {

    /** Spring JPA */
    List<UserAuthModel> findByRoleId(int roleId);

    /** SP Call */
    @Query(nativeQuery = true, value = "exec getUserAuthDetailsListWhoseOtpCountGreaterThanThree @startOfToday = :startOfToday")
    List<UserAuthModel> getUserListWhoseOtpCountGreaterThanThree(LocalDateTime startOfToday);

    /** SP Call */
    @Query(nativeQuery = true, value = "exec getUserAuthDetailsByUsername @username = :email")
    Optional<UserAuthModel> getUserDetailsByUsername(String email);

    /** SP Call */
    @Query(nativeQuery = true, value = "exec updateRecurringIncomesAndExpenses")
    @Transactional
    @Modifying
    void updateRecurringIncomesAndExpenses();

    /** SQL Native Query */
    @Query(value = """
            SELECT uat.*
            FROM user_auth_table uat WITH (NOLOCK)
            INNER JOIN user_auth_hist_table uaht WITH (NOLOCK) ON uaht.user_id = uat.id
            WHERE uat.is_deleted = 1
                 AND uaht.reason_type_id = :reasonTypeId
                 AND uat.role_id = :roleId
                 AND uaht.updated_time < DATEADD(DAY, -30, GETDATE())
                 AND NOT EXISTS (
                    SELECT 1
                    FROM contact_us_table cut WITH (NOLOCK)
                    WHERE cut.email = uat.username
                         AND cut.request_reason = 'ACCOUNT_NOT_DELETE_REQUEST'
                         AND cut.request_status = 'SUBMITTED'
                         AND cut.is_request_active = 1
                         AND cut.is_verified = 0
                 )""", nativeQuery = true)
    List<UserAuthModel> getDeletedUsersList(@Param("roleId") int roleId, @Param("reasonTypeId") int reasonTypeId);

    /** SQL Native Query */
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

    /** SQL Native Query */
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = """
            INSERT INTO user_profile_details_table (user_id, name, created_date, address, income_range)
            VALUES (:userId, :name, :createdTime, :address, 0)
            """)
    void insertProfileDetailsDuringSignup(@Param("userId") Long userId, @Param("name") String name, @Param("createdTime") LocalDateTime createdTime, @Param("address") String address);

    /** SQL Native Query */
    @Query(nativeQuery = true, value = """
            SELECT cut.* 
            FROM contact_us_table cut WITH (NOLOCK)
            WHERE cut.email = :username
            """)
    List<ContactUsProjection> getContactUsRecordsByUsername(@Param("username") String username);

    /** SQL Native Query */
    @Query(nativeQuery = true, value = """
            SELECT updt.* 
            FROM user_profile_details_table updt WITH (NOLOCK)
            WHERE updt.user_id = :userId
            """)
    List<ProfileDetailsProjection> getUserProfileDetailsByUserId(@Param("userId") Long userId);

    /** SQL Native Query */
    @Query(nativeQuery = true, value = """
            SELECT cuth.* 
            FROM contact_us_table_hist cuth WITH (NOLOCK)
            WHERE cuth.contact_ud_id = :contactUsId
            """)
    List<ContactUsHistProjection> getContactUsHistoryDetailsByContactUsId(@Param("contactUsId") Long contactUsId);

    /** ORM Mapping */
    @Query(name = "UserProfile.getUserNameByUsername", nativeQuery = true)
    String getUserNameByUsername(@Param("username") String username);
}
