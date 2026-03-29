package com.moneyfi.transaction.model.income;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "income_table")
@Builder
public class IncomeModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    private Long userId;
    @Column(precision = 38, scale = 2)
    private BigDecimal amount;
    @NotBlank
    private String source;
    private LocalDateTime date;
    @NotNull
    private Integer categoryId;
    private Boolean recurring;
    private Boolean isDeleted;
    private String description;
    private String entryMode;
    private LocalDateTime gmailSyncDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void initFunction() {
        LocalDateTime currentTime = LocalDateTime.now();
        if (isDeleted == null) this.isDeleted = false;
        this.createdAt = currentTime;
        this.updatedAt = currentTime;
    }
}
