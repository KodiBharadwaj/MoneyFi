package com.moneyfi.apigateway.repository;

import com.moneyfi.apigateway.model.ProfileModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<ProfileModel, Long> {

    ProfileModel findByUserId(Long userId);
}
