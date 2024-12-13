package com.finance.user.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class IncomeModel {
    private int id;
    private int userId;
    private double amount;
    private String source;
    private LocalDate date;
    private String category;
    private boolean recurring;

}

