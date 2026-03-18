package com.moneyfi.apigateway.repository.user.auth;

import com.moneyfi.apigateway.model.auth.OtpTempModel;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OtpTempRepository extends JpaRepository<OtpTempModel, Long> {

    /** Spring JPA */
    List<OtpTempModel> findByEmail(String email);

    /** SQL Native Query */
    @Transactional
    @Modifying
    @Query("DELETE FROM OtpTempModel o WHERE o.email = :email AND o.otpType = :requestType")
    int deleteByEmailAndRequestType(String email, String requestType);
}
