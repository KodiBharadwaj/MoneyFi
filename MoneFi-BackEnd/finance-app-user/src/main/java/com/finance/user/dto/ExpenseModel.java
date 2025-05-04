package com.finance.user.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ExpenseModel {

    private int id;
    private int userId;
    private String category;
    private double amount;
    private LocalDate date;
    private boolean recurring;
    private String description;

}
