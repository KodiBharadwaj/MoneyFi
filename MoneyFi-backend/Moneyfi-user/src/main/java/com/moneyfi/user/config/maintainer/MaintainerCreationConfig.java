package com.moneyfi.user.config.maintainer;

import com.moneyfi.constants.enums.LoginMode;
import com.moneyfi.constants.enums.UserRoles;
import com.moneyfi.user.model.primary.auth.UserAuthModel;
import com.moneyfi.user.repository.primary.auth.UserRepository;
import com.moneyfi.user.util.constants.StringConstants;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MaintainerCreationConfig {

    @Value("${app.maintainer.username}")
    private String maintainerUsername;

    @Value("${app.maintainer.password}")
    private String maintainerPassword;

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    @PostConstruct
    public void addMaintainerAtApplicationStartUp() {
        log.info("Maintainer verification completed for username: {}",
                userRepository.getUserDetailsByUsername(maintainerUsername)
                .map(UserAuthModel::getUsername)
                .orElseGet(() -> userRepository.save(
                        UserAuthModel.builder()
                                .username(maintainerUsername)
                                .password(encoder.encode(maintainerPassword))
                                .loginCodeValue(LoginMode.SERVICE_CONFIGURATION.getLoginProcessCode())
                                .roleId(StringConstants.functionToGetRoleIdBasedOnRoleName(UserRoles.MAINTAINER.name()))
                                .build()
                ).getUsername())
        );
    }
}
