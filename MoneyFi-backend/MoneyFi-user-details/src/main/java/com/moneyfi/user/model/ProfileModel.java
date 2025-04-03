package com.moneyfi.user.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ProfileModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int userId;
    private String name;
    private String email;
    private String phone;
    private String address;
    private double incomeRange;
    @Column(columnDefinition = "TEXT")
    private String profileImage;
}