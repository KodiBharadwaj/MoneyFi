package com.moneyfi.user.repository.general;

import com.moneyfi.user.model.general.ReasonDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReasonDetailsRepository extends JpaRepository<ReasonDetails, Integer> {

    /** Spring JPA */
    List<ReasonDetails> findByReasonCode(int reasonCode);
}
