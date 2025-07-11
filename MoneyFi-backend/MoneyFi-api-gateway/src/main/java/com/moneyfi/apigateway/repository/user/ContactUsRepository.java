package com.moneyfi.apigateway.repository.user;

import com.moneyfi.apigateway.model.common.ContactUs;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactUsRepository extends JpaRepository<ContactUs, Long> {
    List<ContactUs> findByEmail(String username);
}
