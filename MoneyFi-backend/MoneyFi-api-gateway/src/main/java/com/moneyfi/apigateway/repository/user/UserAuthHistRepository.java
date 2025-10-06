package com.moneyfi.apigateway.repository.user;

import com.moneyfi.apigateway.model.common.UserAuthHist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAuthHistRepository extends JpaRepository<UserAuthHist, Long> {
    List<UserAuthHist> findByUserId(Long id);

    @Query(value = """
            SELECT uaht.* 
            FROM user_auth_hist_table uaht WITH (NOLOCK)
            WHERE uaht.user_id = :userId 
              AND uaht.reason_type_id = :reasonTypeId
            ORDER BY uaht.updated_time ASC
            """, nativeQuery = true)
    List<UserAuthHist> findTopByUserIdAndReasonTypeId(@Param("userId") Long userId, @Param("reasonTypeId") Integer reasonTypeId);

}
