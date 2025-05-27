package com.moneyfi.goal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
    @Column(precision = 38, scale = 5)
    private BigDecimal currentAmount;
    @Column(precision = 38, scale = 5)
    private BigDecimal targetAmount;
    private LocalDate deadLine;
    private String category;
    private boolean isDeleted;

    @Column(name = "expense_ids")
    private String expenseIds;

}
