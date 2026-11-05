package com.moneyfi.user.service.primary.jwtservice.impl;

import com.moneyfi.user.exceptions.ResourceNotFoundException;
import com.moneyfi.user.model.primary.auth.UserPrincipal;
import com.moneyfi.user.repository.primary.auth.UserRepository;
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
        return new UserPrincipal(userRepository.getUserDetailsByUsername(username).orElseThrow(()-> new ResourceNotFoundException("User not found")));
    }
}
