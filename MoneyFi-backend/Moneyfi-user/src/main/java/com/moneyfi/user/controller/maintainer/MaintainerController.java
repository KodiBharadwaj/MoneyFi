package com.moneyfi.user.controller.maintainer;

import com.moneyfi.constants.enums.UserRoles;
import com.moneyfi.user.service.primary.user.UserAuthService;
import com.moneyfi.user.service.primary.maintainer.MaintainerService;
import com.moneyfi.user.service.primary.maintainer.dto.request.CreateOrUpdateSupportUserCreationRequestDto;
import com.moneyfi.user.service.primary.maintainer.dto.response.SupportUsersResponseDto;
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

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/maintainer")
@PreAuthorize("hasRole('MAINTAINER')")
public class MaintainerController {

    private final MaintainerService maintainerService;
    private final UserAuthService userAuthService;

    @Operation(summary = "Api to add support users", description = "Maintainer can add admin, customer agents")
    @PostMapping("/support-user/add")
    public void addUsersByMaintainer(Authentication authentication,
                                     @RequestBody @Valid CreateOrUpdateSupportUserCreationRequestDto requestDto) {
        Long userId = userAuthService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        maintainerService.addUsersByMaintainer(requestDto, userId);
    }

    @Operation(summary = "Api to fetch support users", description = "Maintainer can fetch admin/customer care users")
    @GetMapping("/support-users/get")
    public ResponseEntity<List<SupportUsersResponseDto>> getSupportUsers(@NotBlank @RequestParam(value = "type") String type,
                                                                         @NotNull @RequestParam(value = "role") UserRoles role) {
        return ResponseEntity.ok(maintainerService.getSupportUsersList(type, role));
    }

    @Operation(summary = "Api to update support user details", description = "Maintainer can update admin/customer care user details")
    @PostMapping("/{id}/support-user/update")
    public void updateSupportUser(Authentication authentication,
                                  @NotNull @PathVariable(value = "id") Long supportUserId,
                                  @RequestBody @Valid CreateOrUpdateSupportUserCreationRequestDto requestDto) {
        Long userId = userAuthService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        maintainerService.updateSupportUser(requestDto, supportUserId, userId);
    }

    @Operation(summary = "Api to update support user details", description = "Maintainer can unblock or retrieve admin/customer care user")
    @PostMapping("/{id}/retrieve-support-user")
    public void unblockOrRetrieveSupportUser(Authentication authentication,
                                             @NotNull @PathVariable(value = "id") Long id,
                                             @NotBlank @RequestParam(value = "type") String type) {
        Long userId = userAuthService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        maintainerService.unblockOrRetrieveSupportUser(id, userId, type);
    }

    @Operation(summary = "Api to delete support user", description = "Maintainer can delete admin/customer care user")
    @DeleteMapping("/{id}/delete-support-user")
    public void deleteSupportUser(Authentication authentication,
                                  @NotNull @PathVariable(value = "id") Long supportUserId,
                                  @NotBlank @RequestParam(value = "type") String type) {
        Long userId = userAuthService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        maintainerService.blockOrDeleteSupportUser(supportUserId, userId, type);
    }
}
