package com.moneyfi.user.repository;

import com.moneyfi.user.model.ReasonDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReasonDetailsRepository extends JpaRepository<ReasonDetails, Integer> {
    List<ReasonDetails> findByReasonCode(int reasonCode);
}
