package com.moneyfi.goal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "goal_table")
public class GoalModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private int userId;
    private String goalName;
    private double currentAmount;
    private double targetAmount;
    private LocalDate deadLine;
    private String category;
}
