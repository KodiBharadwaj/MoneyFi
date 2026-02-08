package com.moneyfi.user.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static com.moneyfi.user.util.constants.StringConstants.CURRENT_DATE_TIME;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reason_type_table")
public class ReasonDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String reason;
    private int reasonCode;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private Boolean isDeleted;
    private Long createdBy;
    private Long updatedBy;

    @PrePersist
    public void prePersist(){
        if(this.isDeleted == null){
            isDeleted = false;
        }
        this.createdTime = CURRENT_DATE_TIME;
        this.updatedTime = CURRENT_DATE_TIME;
    }
}
