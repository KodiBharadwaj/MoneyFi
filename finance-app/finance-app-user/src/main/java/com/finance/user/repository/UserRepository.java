package com.finance.user.repository;

import com.finance.user.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<UserModel, Integer> {

//    @Query("select u from UserModel u where u.email = :username or u.phoneNo = :username")
//    public UserModel findByUsername(String username);

//    @Query("select u.userId from UserModel u where u.email = :email")
//    public Integer getUserIdFromEmail(String email);
      @Query("SELECT u.userId FROM UserModel u WHERE u.email = :email")
      Integer getUserIdFromEmail(@Param("email") String email);




}
