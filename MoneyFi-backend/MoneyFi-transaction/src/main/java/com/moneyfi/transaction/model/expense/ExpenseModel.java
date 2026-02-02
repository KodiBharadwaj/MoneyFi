package com.moneyfi.transaction.model.expense;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.moneyfi.transaction.utils.StringConstants.CURRENT_DATE_TIME;

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
    private Integer categoryId;
    @Column(precision = 38, scale = 2)
    private BigDecimal amount;
    private LocalDateTime date;
    private boolean recurring;
    private String description;
    private String entryMode;
    private boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void initFunction() {
        this.isDeleted = false;
        this.createdAt = CURRENT_DATE_TIME;
        this.updatedAt = CURRENT_DATE_TIME;
    }
}
