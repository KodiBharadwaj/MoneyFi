package com.moneyfi.user.controller.user;

import com.moneyfi.constants.enums.UserRoles;
import com.moneyfi.user.exceptions.ScenarioNotPossibleException;
import com.moneyfi.user.service.user.CommonService;
import com.moneyfi.user.service.jwtservice.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/user-service/sse-notifications")
@RequiredArgsConstructor
public class NotificationSseController {

    private final JwtService jwtService;
    private final CommonService commonService;

    @Operation(summary = "Api to handle SSE mode for updated notifications")
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeForNotification(@NotBlank @RequestParam(value = "token") String token) {
        if (!jwtService.validateTokenOnly(token)) {
            throw new ScenarioNotPossibleException("Invalid or expired token");
        }
        String role = jwtService.extractRole(token);
        if (!UserRoles.USER.name().equals(role)) {
            throw new AuthorizationServiceException("Access denied");
        }
        String username = jwtService.extractUserName(token);
        return commonService.addEmitterForNotification(username);
    }
}
