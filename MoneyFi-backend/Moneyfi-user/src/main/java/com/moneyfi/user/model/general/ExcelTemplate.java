package com.moneyfi.user.model.general;

import com.moneyfi.user.service.admin.dto.response.ExcelTemplateList;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
@Table(name = "excel_template")
@SqlResultSetMapping(
        name = "ExcelTemplateListMapping",
        classes = @ConstructorResult(
                targetClass = ExcelTemplateList.class,
                columns = {
                        @ColumnResult(name = "excelType", type = String.class),
                        @ColumnResult(name = "excelFile", type = byte[].class),
                        @ColumnResult(name = "createdBy", type = String.class),
                        @ColumnResult(name = "updatedBy", type = String.class),
                        @ColumnResult(name = "createdAt", type = LocalDateTime.class),
                        @ColumnResult(name = "updatedAt", type = LocalDateTime.class)
                }
        )
)
public class ExcelTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @Lob
    private byte[] content;

    private String contentType;

    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    @PrePersist
    private void function() {
        LocalDateTime currentTime = LocalDateTime.now();
        this.createdTime = currentTime;
        this.updatedTime = currentTime;
    }
}
