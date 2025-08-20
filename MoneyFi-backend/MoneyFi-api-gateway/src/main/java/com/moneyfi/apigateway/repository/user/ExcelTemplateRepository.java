package com.moneyfi.apigateway.repository.user;

import com.moneyfi.apigateway.model.common.ExcelTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExcelTemplateRepository extends JpaRepository<ExcelTemplate, Integer> {
}
