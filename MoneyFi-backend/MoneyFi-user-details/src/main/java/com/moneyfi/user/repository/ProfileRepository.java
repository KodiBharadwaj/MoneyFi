package com.moneyfi.user.repository;

import com.moneyfi.user.model.ProfileModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProfileRepository extends JpaRepository<ProfileModel,Long> {

    public ProfileModel findByUserId(Long userId);

    @Query("select p.name from ProfileModel p where p.userId = :userId")
    public String getNameFromUserId(Long userId);

}
