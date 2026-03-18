package com.moneyfi.user.repository;

import com.moneyfi.user.model.ExcelTemplate;
import com.moneyfi.user.service.admin.dto.response.ExcelTemplateList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExcelTemplateRepository extends JpaRepository<ExcelTemplate, Integer> {

    /** JPQL */
    @Query("SELECT e FROM ExcelTemplate e WHERE e.name = :fileName")
    Optional<ExcelTemplate> findByName(@Param("fileName") String fileName);

    /** ORM Mapping */
    @Query(name = "getAllExcelTemplatesList", nativeQuery = true)
    List<ExcelTemplateList> getAllExcelTemplateList();
}
