package com.moneyfi.user.repository;

import com.moneyfi.user.model.ProfileModel;
import com.moneyfi.user.model.dto.interfaces.OtpTempProjection;
import com.moneyfi.user.model.dto.interfaces.UserAuthHistProjection;
import com.moneyfi.user.model.dto.interfaces.UserAuthProjection;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<ProfileModel, Long> {

    Optional<ProfileModel> findByUserId(Long userId);

    List<ProfileModel> findByPhone(String phoneNumber);

    @Query(nativeQuery = true, value = "exec getUsersByUsingUserProfileDetails @dateOfBirth = :dateOfBirth, @name = :name," +
            "@gender = :gender, @maritalStatus = :maritalStatus")
    List<ProfileModel> findByUserProfileDetails(LocalDate dateOfBirth, String name, String gender, String maritalStatus);

    @Query(nativeQuery = true, value =  "exec getUserIdFromUsernameAndToken @username = :username, @token = :token")
    Long getUserIdFromUsernameAndToken(String username, String token);

    @Query(
            nativeQuery = true,
            value = "exec getUserAuthDetailsByUsername @username = :username"
    )
    Optional<UserAuthProjection> getUserDetailsByUsername(String username);

    @Query(nativeQuery = true, value = """
            SELECT updt.* 
            FROM user_profile_details_table updt WITH (NOLOCK)
            INNER JOIN user_auth_table uat WITH (NOLOCK) ON uat.id = updt.user_id
            WHERE uat.username = :username
            """)
    Optional<ProfileModel> findByUserEmail(@Param(("username")) String username);

    @Query(value = """
            SELECT uaht.* 
            FROM user_auth_hist_table uaht WITH (NOLOCK)
            WHERE uaht.user_id = :userId 
              AND uaht.reason_type_id = :reasonTypeId
            ORDER BY uaht.updated_time ASC
            """, nativeQuery = true)
    List<UserAuthHistProjection> findTopByUserIdAndReasonTypeId(@Param("userId") Long userId, @Param("reasonTypeId") Integer reasonTypeId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = """
        UPDATE user_auth_table SET is_blocked = :blockStatus WHERE id = :id
        """)
    void updateUserAuthTableWithBlockOrUnblockStatus(@Param("id") Long id, @Param("blockStatus") boolean blockStatus);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = """
        UPDATE user_auth_table SET is_deleted = :blockStatus WHERE id = :id
        """)
    void updateUserAuthTableWithDeleteOrUndeleteStatus(@Param("id") Long id, @Param("blockStatus") boolean blockStatus);

    @Transactional
    @Modifying
    @Query(value = """
        INSERT INTO user_auth_hist_table 
            (user_id, updated_time, reason_type_id, comment, updated_by)
        VALUES 
            (:userId, :updatedTime, :reasonTypeId, :comment, :updatedBy)
        """, nativeQuery = true)
    void insertUserAuthHistory(@Param("userId") Long userId, @Param("updatedTime") LocalDateTime updatedTime,
                               @Param("reasonTypeId") int reasonTypeId, @Param("comment") String comment, @Param("updatedBy") Long updatedBy);

    @Query(nativeQuery = true, value = """
            SELECT ott.* 
            FROM otp_temp_table ott WITH (NOLOCK)
            WHERE ott.email = :username
                AND ott.otp_type = :otpType
                AND ott.expiration_time > :currentTime
            """)
    Optional<OtpTempProjection> getOtpTempDetails(@Param("username") String username, @Param("otpType") String otpType, @Param("currentTime") LocalDateTime currentTime);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM otp_temp_table WHERE email = :email AND otp_type = :requestType")
    int deleteByEmailAndRequestType(@Param("email") String email, @Param("requestType") String requestType);

    @Query(nativeQuery = true, value = """
            SELECT uat.* 
            FROM user_auth_table uat WITH (NOLOCK)
            WHERE uat.id = :userId
            """)
    Optional<UserAuthProjection> getUserAuthModelByUserId(@Param("userId") Long userId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "UPDATE user_gmail_auth set count = :gmailSyncRequestCount WHERE user_id = :userId")
    int updateGmailSyncCountByUserRequest(@Param(value = "userId") Long userId, @Param(value = "gmailSyncRequestCount") int gmailSyncRequestCount);
}
