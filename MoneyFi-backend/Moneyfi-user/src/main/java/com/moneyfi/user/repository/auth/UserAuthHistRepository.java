package com.moneyfi.user.repository.auth;

import com.moneyfi.user.model.auth.UserAuthHist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAuthHistRepository extends JpaRepository<UserAuthHist, Long> {

    /** Spring JPA */
    List<UserAuthHist> findByUserId(Long id);

    /** JPQL */
    @Query(value = "SELECT u FROM UserAuthHist u WHERE u.userId = :userId AND u.reasonTypeId = :reasonTypeId ORDER BY u.updatedTime ASC")
    List<UserAuthHist> findTopByUserIdAndReasonTypeId(@Param("userId") Long userId, @Param("reasonTypeId") Integer reasonTypeId);
}
