package com.moneyfi.user.repository.general;

import com.moneyfi.user.model.general.ContactUsHist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactUsHistRepository extends JpaRepository<ContactUsHist, Long> {

    /** Spring JPA */
    List<ContactUsHist> findByContactUsId(Long id);

    /** JPQL */
    @Query("SELECT c FROM ContactUsHist c WHERE c.contactUsId = :id AND c.requestStatus = 'SUBMITTED'")
    List<ContactUsHist> findByContactUsIdList(Long id);
}
