package com.moneyfi.apigateway.repository.auth;

import com.moneyfi.apigateway.model.auth.OtpTempModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpTempRepository extends JpaRepository<OtpTempModel, Long> {

    OtpTempModel findByEmail(String email);
}
