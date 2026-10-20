package com.moneyfi.transaction.batch.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

import static com.moneyfi.constants.constants.CommonConstants.MDC_REQUEST_ID;
import static com.moneyfi.constants.constants.CommonConstants.REQUEST_ID_HEADER;

@Component
public class RequestIdFilterConfig extends OncePerRequestFilter {

    private static final Logger log =
            LoggerFactory.getLogger(RequestIdFilterConfig.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String requestId = request.getHeader(REQUEST_ID_HEADER);

        if (requestId == null || requestId.isBlank()) requestId = UUID.randomUUID().toString();

        try {
            // Make the ID available to all logs on this request thread
            MDC.put(MDC_REQUEST_ID, requestId);

            // Include the ID in the response if useful for debugging
            response.setHeader(REQUEST_ID_HEADER, requestId);

            log.info("Incoming request: method={}, uri={}", request.getMethod(), request.getRequestURI());

            filterChain.doFilter(request, response);

        } finally {
            // Prevent request IDs leaking between reused server threads
            MDC.remove(MDC_REQUEST_ID);
        }
    }
}
