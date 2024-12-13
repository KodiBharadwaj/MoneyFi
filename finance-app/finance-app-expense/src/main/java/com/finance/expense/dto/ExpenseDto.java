package com.finance.expense.dto;

import lombok.Data;

@Data
public class ExpenseDto {
    private String category;
    private double totalSpent;

    public ExpenseDto(String category, double totalSpent) {
        this.category = category;
        this.totalSpent = totalSpent;
    }
}
