package com.moneyfi.apigateway.repository.user;

import com.moneyfi.apigateway.model.common.UserAuthHist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAuthHistRepository extends JpaRepository<UserAuthHist, Long> {
}
