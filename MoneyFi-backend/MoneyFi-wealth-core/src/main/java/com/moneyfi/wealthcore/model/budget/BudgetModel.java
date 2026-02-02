package com.moneyfi.wealthcore.model.budget;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.moneyfi.wealthcore.utils.StringConstants.CURRENT_DATE_TIME;

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
    private Integer categoryId;
    @Column(precision = 38, scale = 2)
    private BigDecimal currentSpending;
    @Column(precision = 38, scale = 2)
    private BigDecimal moneyLimit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void function() {
        createdAt = CURRENT_DATE_TIME;
        updatedAt = CURRENT_DATE_TIME;
    }
}
