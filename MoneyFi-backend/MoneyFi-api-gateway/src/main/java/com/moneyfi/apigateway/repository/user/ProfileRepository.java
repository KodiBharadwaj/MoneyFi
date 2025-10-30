package com.moneyfi.apigateway.repository.user;

import com.moneyfi.apigateway.model.common.ProfileModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<ProfileModel, Long> {

    Optional<ProfileModel> findByUserId(Long userId);

    List<ProfileModel> findByPhone(String phoneNumber);

    @Query(nativeQuery = true, value = "exec getUsersByUsingUserProfileDetails @dateOfBirth = :dateOfBirth, @name = :name," +
            "@gender = :gender, @maritalStatus = :maritalStatus")
    List<ProfileModel> findByUserProfileDetails(LocalDate dateOfBirth, String name, String gender, String maritalStatus);
}
