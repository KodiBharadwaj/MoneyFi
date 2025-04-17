package com.moneyfi.apigateway.repository;

import com.moneyfi.apigateway.model.OtpTempModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpTempRepository extends JpaRepository<OtpTempModel, Long> {

    OtpTempModel findByEmail(String email);
}
