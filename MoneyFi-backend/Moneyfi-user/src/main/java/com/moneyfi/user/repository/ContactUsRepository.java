package com.moneyfi.user.repository;

import com.moneyfi.user.model.ContactUs;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactUsRepository extends JpaRepository<ContactUs, Long> {
    List<ContactUs> findByEmail(String username);
}
