package com.moneyfi.user.repository;

import com.moneyfi.user.model.ProfileModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProfileRepository extends JpaRepository<ProfileModel,Long> {

    @Query(nativeQuery = true, value = "exec getProfileDetailsByUserId @userId = :userId")
    ProfileModel findByUserId(Long userId);

    @Query(nativeQuery = true, value = "exec getNameFromProfileModelByUserId @userId = :userId")
    String getNameFromUserId(Long userId);

}
