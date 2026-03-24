package com.moneyfi.user.repository.auth;

import com.moneyfi.user.model.auth.OtpTempModel;
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
public interface OtpTempRepository extends JpaRepository<OtpTempModel, Long> {

    /** Spring JPA */
    List<OtpTempModel> findByEmail(String email);

    /** SQL Native Query */
    @Query(nativeQuery = true, value = """
            SELECT ott.* 
            FROM otp_temp_table ott WITH (NOLOCK)
            WHERE ott.email = :username
                AND ott.otp_type = :otpType
                AND ott.expiration_time > :currentTime
            """)
    Optional<OtpTempModel> getOtpTempDetails(@Param("username") String username, @Param("otpType") String otpType, @Param("currentTime") LocalDateTime currentTime);

    /** SQL Native Query */
    @Transactional
    @Modifying
    @Query("DELETE FROM OtpTempModel o WHERE o.email = :email AND o.otpType = :requestType")
    int deleteByEmailAndRequestType(String email, String requestType);
}
