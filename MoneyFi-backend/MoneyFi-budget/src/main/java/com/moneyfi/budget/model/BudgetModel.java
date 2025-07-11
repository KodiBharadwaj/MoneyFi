package com.moneyfi.budget.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "budget_table")
public class BudgetModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String category;
    @Column(precision = 38, scale = 2)
    private BigDecimal currentSpending;
    @Column(precision = 38, scale = 2)
    private BigDecimal moneyLimit;

}
