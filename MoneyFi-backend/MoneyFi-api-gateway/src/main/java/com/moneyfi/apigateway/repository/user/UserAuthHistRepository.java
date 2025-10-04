package com.moneyfi.apigateway.repository.user;

import com.moneyfi.apigateway.model.common.UserAuthHist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAuthHistRepository extends JpaRepository<UserAuthHist, Long> {
    List<UserAuthHist> findByUserId(Long id);
}
