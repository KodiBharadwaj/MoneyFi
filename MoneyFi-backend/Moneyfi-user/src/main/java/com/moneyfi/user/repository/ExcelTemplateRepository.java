package com.moneyfi.user.repository;

import com.moneyfi.user.model.ExcelTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExcelTemplateRepository extends JpaRepository<ExcelTemplate, Integer> {

    @Query("SELECT e FROM ExcelTemplate WHERE e.name = :fileName")
    Optional<ExcelTemplate> findByName(@Param("fileName") String fileName);
}
