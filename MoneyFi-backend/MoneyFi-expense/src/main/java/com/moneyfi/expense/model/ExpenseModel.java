package com.moneyfi.expense.model;

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
@Table(name = "expense_table")
public class ExpenseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String category;
    @Column(precision = 38, scale = 10)
    private BigDecimal amount;
    private LocalDate date;
    private boolean recurring;
    private String description;
    private boolean is_deleted;

}
