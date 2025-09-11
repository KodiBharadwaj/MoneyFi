package com.moneyfi.apigateway.repository.user;

import com.moneyfi.apigateway.model.common.ReasonDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReasonDetailsRepository extends JpaRepository<ReasonDetails, Long> {
}
