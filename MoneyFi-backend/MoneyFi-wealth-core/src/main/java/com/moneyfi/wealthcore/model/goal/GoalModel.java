package com.moneyfi.wealthcore.model.goal;

import jakarta.persistence.*;
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
@Table(name = "goal_table")
@Builder(toBuilder = true)
public class GoalModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "goal_name", nullable = false)
    private String goalName;

    @Column(name = "current_amount", nullable = false, precision = 38, scale = 2)
    private BigDecimal currentAmount;

    @Column(name = "recurring_amount", nullable = false, precision = 38, scale = 2)
    private BigDecimal recurringAmount;

    @Column(name = "target_amount", nullable = false, precision = 38, scale = 2)
    private BigDecimal targetAmount;

    @Column(name = "dead_line_date", nullable = false)
    private LocalDateTime deadLine;

    @Column(name = "category_id", nullable = false)
    private Integer categoryId;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void initFunction() {
        LocalDateTime currentTime = LocalDateTime.now();
        this.deleted = Boolean.FALSE;
        this.createdAt = currentTime;
        this.updatedAt = currentTime;
    }
}
