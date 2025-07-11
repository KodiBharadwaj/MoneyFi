package com.moneyfi.apigateway.service.jwtservice.impl;

import com.moneyfi.apigateway.model.auth.UserAuthModel;
import com.moneyfi.apigateway.repository.user.auth.UserRepository;
import com.moneyfi.apigateway.model.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public MyUserDetailsService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserAuthModel userAuthModel = userRepository.getUserDetailsByUsername(username);

        if (userAuthModel == null) {
            log.info("No userAuthModel with this UserAuthModel Name: " + username);
            throw new UsernameNotFoundException("UserAuthModel Not Found");
        } else {
            return new UserPrincipal(userAuthModel);
        }
    }
}
