package com.moneyfi.goal.model;

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
    private String category;
    private boolean isDeleted;

    @Column(name = "expense_ids")
    private String expenseIds;

}
