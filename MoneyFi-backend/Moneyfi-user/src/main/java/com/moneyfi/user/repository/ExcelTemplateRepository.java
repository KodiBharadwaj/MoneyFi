package com.moneyfi.user.repository;

import com.moneyfi.user.model.ExcelTemplate;
import com.moneyfi.user.model.dto.interfaces.ExcelTemplateListProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExcelTemplateRepository extends JpaRepository<ExcelTemplate, Integer> {

    @Query("SELECT e FROM ExcelTemplate e WHERE e.name = :fileName")
    Optional<ExcelTemplate> findByName(@Param("fileName") String fileName);

    @Query(nativeQuery = true, value = """
            SELECT et.name AS excelType
            	,et.content AS excelFile
            	,uatA.username AS createdBy
            	,uatB.username AS updatedBy
            	,et.created_time AS createdAt
            	,et.updated_time AS updatedAt
            FROM excel_template et WITH (NOLOCK)
            INNER JOIN user_auth_table uatA WITH (NOLOCK) ON uatA.id = et.created_by
            INNER JOIN user_auth_table uatB WITH (NOLOCK) ON uatB.id = et.updated_by
            """)
    List<ExcelTemplateListProjection> getAllExcelTemplateList();
}
