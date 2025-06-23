package com.moneyfi.apigateway.repository.auth;

import com.moneyfi.apigateway.model.auth.OtpTempModel;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface OtpTempRepository extends JpaRepository<OtpTempModel, Long> {

    OtpTempModel findByEmail(String email);

    @Transactional
    @Modifying
    void deleteByEmail(String email);
}
