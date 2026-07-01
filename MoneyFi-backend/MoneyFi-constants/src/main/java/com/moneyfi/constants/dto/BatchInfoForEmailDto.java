package com.moneyfi.constants.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public class BatchInfoForEmailDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String transactionType;
    private String description;
    private BigDecimal amount;
    private String name;
    private String username;
    private Long userId;

    public BatchInfoForEmailDto(String transactionType, String description, BigDecimal amount, String name, String username, Long userId) {
        this.transactionType = transactionType;
        this.description = description;
        this.amount = amount;
        this.name = name;
        this.username = username;
        this.userId = userId;
    }

    public BatchInfoForEmailDto() {
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "BatchInfoForEmailDto{" +
                "transactionType='" + transactionType + '\'' +
                ", description='" + description + '\'' +
                ", amount=" + amount +
                ", name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", userId=" + userId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BatchInfoForEmailDto that = (BatchInfoForEmailDto) o;
        return Objects.equals(transactionType, that.transactionType) && Objects.equals(description, that.description) && Objects.equals(amount, that.amount) && Objects.equals(name, that.name) && Objects.equals(username, that.username) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionType, description, amount, name, username, userId);
    }
}
