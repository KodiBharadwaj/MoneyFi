package com.moneyfi.user.repository.general;

import com.moneyfi.user.model.general.ContactUs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactUsRepository extends JpaRepository<ContactUs, Long> {

    /** Spring JPA */
    List<ContactUs> findByEmail(String username);
}
