package com.moneyfi.user.repository.secondary;

import com.moneyfi.user.model.secondary.SupportAgentWorkload;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupportAgentWorkloadRepository extends MongoRepository<SupportAgentWorkload, String> {
    Optional<SupportAgentWorkload> findByAgentId(Long userId);
}
