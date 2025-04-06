package com.moneyfi.income.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

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
    @Column(precision = 38, scale = 10)
    private BigDecimal amount;
    private String source;
    private LocalDate date;
    private String category;
    private boolean recurring;
    private boolean is_deleted;
}
