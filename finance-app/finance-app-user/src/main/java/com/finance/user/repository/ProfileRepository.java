package com.finance.user.repository;

import com.finance.user.model.ProfileModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProfileRepository extends JpaRepository<ProfileModel,Integer> {

    public ProfileModel findByUserId(int userId);

    @Query("select p.name from ProfileModel p where p.userId = :userId")
    public String getNameFromUserId(int userId);

}
