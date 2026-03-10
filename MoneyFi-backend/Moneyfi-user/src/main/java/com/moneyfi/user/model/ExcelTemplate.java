package com.moneyfi.user.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
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
public class ExcelTemplate {
    @Id
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
