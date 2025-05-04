package com.finance.user.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class GoalModel {
    private int id;
    private int userId;
    private String goalName;
    private double currentAmount;
    private double targetAmount;
    private LocalDate deadLine;
    private String category;
}
