package com.moneyfi.income.model;

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
    private String category;
    private boolean recurring;
    private boolean isDeleted;
}
