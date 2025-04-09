package com.moneyfi.apigateway.service;

import com.moneyfi.apigateway.model.UserAuthModel;
import com.moneyfi.apigateway.repository.UserRepository;
import com.moneyfi.apigateway.model.UserPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public MyUserDetailsService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserAuthModel userAuthModel = userRepository.findByUsername(username);

        if (userAuthModel == null) {
            System.out.println("No userAuthModel with this UserAuthModel Name: " + username);
            throw new UsernameNotFoundException("UserAuthModel Not Found");
        } else {
            return new UserPrincipal(userAuthModel);
        }
    }
}
