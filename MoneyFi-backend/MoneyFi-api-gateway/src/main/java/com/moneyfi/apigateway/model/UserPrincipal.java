package com.moneyfi.apigateway.model;

import com.moneyfi.apigateway.model.auth.UserAuthModel;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

import static com.moneyfi.apigateway.util.constants.StringUtils.userRoleAssociation;


public class UserPrincipal implements UserDetails {

    private final UserAuthModel userAuthModel;

    public UserPrincipal(UserAuthModel userAuthModel) {
        this.userAuthModel = userAuthModel;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = userRoleAssociation.get(userAuthModel.getRoleId());
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return userAuthModel.getPassword();
    }

    @Override
    public String getUsername() {
        return userAuthModel.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
