package com.moneyfi.apigateway.repository.common;

import com.moneyfi.apigateway.model.common.ContactUs;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactUsRepository extends JpaRepository<ContactUs, Long> {
}
