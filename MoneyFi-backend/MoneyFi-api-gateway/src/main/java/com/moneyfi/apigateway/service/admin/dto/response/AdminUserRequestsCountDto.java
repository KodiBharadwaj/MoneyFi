package com.moneyfi.apigateway.service.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.atomic.AtomicInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserRequestsCountDto {
    private AtomicInteger nameChangeActiveRequests = null;
    private AtomicInteger nameChangeCompletedRequests = null;
    private AtomicInteger nameChangeDeclinedRequests = null;

    private AtomicInteger accBlockActiveRequests = null;
    private AtomicInteger accBlockChangeCompletedRequests = null;
    private AtomicInteger accBlockChangeDeclinedRequests = null;

    private AtomicInteger accRetrieveChangeActiveRequests = null;
    private AtomicInteger accRetrieveChangeCompletedRequests = null;
    private AtomicInteger accRetrieveChangeDeclinedRequests = null;
}
