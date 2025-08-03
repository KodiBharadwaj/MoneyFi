package com.moneyfi.apigateway.repository.user;

import com.moneyfi.apigateway.model.common.ContactUsHist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactUsHistRepository extends JpaRepository<ContactUsHist, Long> {
    ContactUsHist findByContactUsId(Long id);

    @Query("SELECT c FROM ContactUsHist c WHERE c.contactUsId = :id AND c.requestStatus = 'SUBMITTED'")
    List<ContactUsHist> findByContactUsIdList(Long id);
}
