package com.moneyfi.constants.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class GoalExpenseRelationRequestDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer categoryId;
    private BigDecimal amount;
    private LocalDateTime date;
    private boolean recurring;
    private String description;
    private Long goalId;

    public GoalExpenseRelationRequestDto() {
    }

    public GoalExpenseRelationRequestDto(Integer categoryId, BigDecimal amount, LocalDateTime date, boolean recurring, String description, Long goalId) {
        this.categoryId = categoryId;
        this.amount = amount;
        this.date = date;
        this.recurring = recurring;
        this.description = description;
        this.goalId = goalId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public boolean isRecurring() {
        return recurring;
    }

    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getGoalId() {
        return goalId;
    }

    public void setGoalId(Long goalId) {
        this.goalId = goalId;
    }

    @Override
    public String toString() {
        return "GoalExpenseRelationRequestDto{" +
                "categoryId=" + categoryId +
                ", amount='" + amount + '\'' +
                ", date='" + date + '\'' +
                ", recurring='" + recurring + '\'' +
                ", description='" + description + '\'' +
                ", goalId='" + goalId + '\'' +
                '}';
    }
}