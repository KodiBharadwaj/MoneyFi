package com.moneyfi.apigateway.repository.user;

import com.moneyfi.apigateway.model.common.ReasonDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReasonDetailsRepository extends JpaRepository<ReasonDetails, Integer> {
    List<ReasonDetails> findByReasonCode(int reasonCode);
}
