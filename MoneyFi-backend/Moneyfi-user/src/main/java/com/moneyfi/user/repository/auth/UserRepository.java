package com.moneyfi.user.repository.auth;

import com.moneyfi.user.model.auth.UserAuthModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserAuthModel, Long> {

    /** Spring JPA */
    List<UserAuthModel> findByRoleId(int roleId);

    /** SP Call */
    @Query(nativeQuery = true, value = "exec getUserAuthDetailsByUsername @username = :email")
    Optional<UserAuthModel> getUserDetailsByUsername(String email);

    /** SP Call */
    @Query(nativeQuery = true, value = "exec updateRecurringIncomesAndExpenses")
    @Transactional
    @Modifying
    void updateRecurringIncomesAndExpenses();

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "exec updateOtpCountToResetForUsers")
    void updateOtpCountToResetForUsers();

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "exec deleteAllUserDataForDeletedUsersAfter30Days")
    void deleteAllUserDataForDeletedUsersAfter30Days();
}
