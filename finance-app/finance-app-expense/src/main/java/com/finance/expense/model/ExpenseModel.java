package com.finance.expense.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "expense_table")
public class ExpenseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private int userId;
    private String category;
    private double amount;
    private LocalDate date;
    private boolean recurring;
    private String description;
    private boolean is_deleted;

}
