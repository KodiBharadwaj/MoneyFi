package com.moneyfi.apigateway.repository.user.auth;

import com.moneyfi.apigateway.model.auth.OtpTempModel;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;

public interface OtpTempRepository extends JpaRepository<OtpTempModel, Long> {

    List<OtpTempModel> findByEmail(String email);

    @Transactional
    @Modifying
    void deleteByEmail(String email);
}
