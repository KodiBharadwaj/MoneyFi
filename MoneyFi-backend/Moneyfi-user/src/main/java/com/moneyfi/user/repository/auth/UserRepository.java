package com.moneyfi.user.repository.auth;

import com.moneyfi.user.model.auth.UserAuthModel;
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
}
