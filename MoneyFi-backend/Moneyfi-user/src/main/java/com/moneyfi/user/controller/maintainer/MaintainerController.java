package com.moneyfi.user.controller.maintainer;

import com.moneyfi.user.service.user.UserAuthService;
import com.moneyfi.user.service.maintainer.MaintainerService;
import com.moneyfi.user.service.maintainer.dto.request.CreateOrUpdateAdminRequestDto;
import com.moneyfi.user.service.maintainer.dto.response.AdminUsersResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user-service/maintainer")
@PreAuthorize("hasRole('MAINTAINER')")
@RequiredArgsConstructor
@Validated
public class MaintainerController {

    private final MaintainerService maintainerService;
    private final UserAuthService userAuthService;

    @Operation(summary = "Api to add admin users")
    @PostMapping("/add-admin")
    public void addAdminUser(Authentication authentication,
                             @RequestBody @Valid CreateOrUpdateAdminRequestDto requestDto) {
        Long userId = userAuthService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        maintainerService.addAdminUser(requestDto, userId);
    }

    @Operation(summary = "Api to get Admin users")
    @GetMapping("/get-admins")
    public ResponseEntity<List<AdminUsersResponseDto>> getAdminUsers(@NotBlank @RequestParam(value = "type") String type) {
        return ResponseEntity.ok(maintainerService.getAdminUsersList(type));
    }

    @Operation(summary = "Api to update Admin user cred")
    @PostMapping("/{id}/update-admin")
    public void updateAdminUser(Authentication authentication,
                                @NotNull @PathVariable(value = "id") Long id,
                                @RequestBody @Valid CreateOrUpdateAdminRequestDto requestDto) {
        Long userId = userAuthService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        maintainerService.updateAdminUser(requestDto, id, userId);
    }

    @Operation(summary = "Api to update Admin user cred")
    @PostMapping("/{id}/retrieve-admin")
    public void unblockOrRetrieveAdmin(Authentication authentication,
                                       @NotNull @PathVariable(value = "id") Long id,
                                       @NotBlank @RequestParam(value = "type") String type) {
        Long userId = userAuthService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        maintainerService.unblockOrRetrieveAdmin(id, userId, type);
    }

    @Operation(summary = "Api to delete Admin user")
    @DeleteMapping("/{id}/delete-admin")
    public void deleteAdminUser(Authentication authentication,
                                @NotNull @PathVariable(value = "id") Long id,
                                @NotBlank @RequestParam(value = "type") String type) {
        Long userId = userAuthService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        maintainerService.deleteAdminUser(id, userId, type);
    }
}
