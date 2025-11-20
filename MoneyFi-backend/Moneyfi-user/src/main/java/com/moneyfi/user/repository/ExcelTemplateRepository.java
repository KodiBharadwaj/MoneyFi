package com.moneyfi.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExcelTemplateRepository extends JpaRepository<com.moneyfi.user.model.ExcelTemplate, Integer> {
}
