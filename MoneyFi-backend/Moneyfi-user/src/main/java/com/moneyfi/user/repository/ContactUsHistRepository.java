package com.moneyfi.user.repository;

import com.moneyfi.user.model.ContactUsHist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactUsHistRepository extends JpaRepository<ContactUsHist, Long> {
    List<ContactUsHist> findByContactUsId(Long id);

    @Query("SELECT c FROM ContactUsHist c WHERE c.contactUsId = :id AND c.requestStatus = 'SUBMITTED'")
    List<ContactUsHist> findByContactUsIdList(Long id);
}
