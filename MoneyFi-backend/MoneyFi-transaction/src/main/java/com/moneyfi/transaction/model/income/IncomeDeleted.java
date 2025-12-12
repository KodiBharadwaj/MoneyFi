package com.moneyfi.transaction.model.income;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "income_table_deleted")
public class IncomeDeleted {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long incomeId;
    private LocalDateTime startDateTime;
    private LocalDateTime expiryDateTime;
    private LocalDateTime deletedAt;
}
