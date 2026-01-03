package com.moneyfi.user.controller.user;

import com.moneyfi.user.config.JwtService;
import com.moneyfi.user.service.common.CommonService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/user-service/user/sse-notifications")
public class NotificationSseController {

    private final JwtService jwtService;
    private final CommonService commonService;

    public NotificationSseController(JwtService jwtService,
                                     CommonService commonService){
        this.jwtService = jwtService;
        this.commonService = commonService;
    }

    @Operation(summary = "Api to handle SSE mode for updated notifications")
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeForNotification(@RequestParam("token") String token) {
        String username = jwtService.extractUsernameFromToken(token);
        return commonService.addEmitterForNotification(username);
    }
}
