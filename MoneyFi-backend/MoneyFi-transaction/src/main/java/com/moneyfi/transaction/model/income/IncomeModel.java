package com.moneyfi.transaction.model.income;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "income_table")
public class IncomeModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    @Column(precision = 38, scale = 2)
    private BigDecimal amount;
    private String source;
    private LocalDateTime date;
    private Integer categoryId;
    private boolean recurring;
    private boolean isDeleted;
    private String description;
    private String entryMode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void initFunction() {
        this.isDeleted = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
