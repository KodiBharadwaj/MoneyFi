package com.moneyfi.user.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ExcelTemplate {
    @Id
    private Integer id;

    private String name;

    @Lob
    private byte[] content;

    private String contentType;
}
