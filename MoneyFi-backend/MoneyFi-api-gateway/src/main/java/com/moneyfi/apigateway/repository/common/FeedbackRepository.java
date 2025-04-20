package com.moneyfi.apigateway.repository.common;

import com.moneyfi.apigateway.model.common.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
}
