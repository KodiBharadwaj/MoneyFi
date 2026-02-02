package com.moneyfi.wealthcore.model.goal;

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
@Table(name = "goal_table")
public class GoalModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String goalName;
    @Column(precision = 38, scale = 2)
    private BigDecimal currentAmount;
    @Column(precision = 38, scale = 2)
    private BigDecimal targetAmount;
    private LocalDateTime deadLine;
    private Integer categoryId;
    private boolean isDeleted;
    private String description;
    @Column(name = "expense_ids")
    private String expenseIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void initFunction() {
        this.isDeleted = false;
        this.createdAt = CURRENT_DATE_TIME;
        this.updatedAt = CURRENT_DATE_TIME;
    }
}
