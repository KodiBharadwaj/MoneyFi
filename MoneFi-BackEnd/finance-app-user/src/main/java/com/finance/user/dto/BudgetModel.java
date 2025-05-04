package com.finance.user.dto;

import lombok.Data;

@Data
public class BudgetModel {
    private int id;
    private int userId;
    private String category;
    private double currentSpending;
    private double moneyLimit;
}
