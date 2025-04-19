package com.moneyfi.apigateway.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_profile_details_table")
public class ProfileModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String name;
    private String email;
    private LocalDate createdDate;
    private String phone;
    private String gender;
    private LocalDate dateOfBirth;
    private String maritalStatus;
    private String address;
    private double incomeRange;
    @Column(columnDefinition = "TEXT")
    private String profileImage;
}