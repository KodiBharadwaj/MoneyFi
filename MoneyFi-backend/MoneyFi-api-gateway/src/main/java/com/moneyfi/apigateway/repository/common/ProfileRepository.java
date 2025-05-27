package com.moneyfi.apigateway.repository.common;

import com.moneyfi.apigateway.model.common.ProfileModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<ProfileModel, Long> {

    ProfileModel findByUserId(Long userId);
}
