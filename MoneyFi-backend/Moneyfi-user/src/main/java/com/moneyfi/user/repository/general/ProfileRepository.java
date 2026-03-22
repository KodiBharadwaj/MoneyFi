package com.moneyfi.user.repository.general;

import com.moneyfi.user.model.general.ProfileModel;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<ProfileModel, Long> {

    /** Spring JPA */
    Optional<ProfileModel> findByUserId(Long userId);

    /** Spring JPA */
    List<ProfileModel> findByPhone(String phoneNumber);

    /** SP Call */
    @Query(nativeQuery = true, value = "exec getUsersByUsingUserProfileDetails @dateOfBirth = :dateOfBirth, @name = :name, " +
            "@gender = :gender, @maritalStatus = :maritalStatus")
    List<ProfileModel> findByUserProfileDetails(LocalDate dateOfBirth, String name, String gender, String maritalStatus);

    /** SQL Native Query */
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM otp_temp_table WHERE email = :email AND otp_type = :requestType")
    int deleteByEmailAndRequestType(@Param("email") String email, @Param("requestType") String requestType);

    /** ORM Mapping */
    @Query(name = "getUserProfileDetails", nativeQuery = true)
    Optional<ProfileModel> findByUserEmail(@Param(("username")) String username);
}
