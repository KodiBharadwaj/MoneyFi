package com.moneyfi.apigateway.service;

import com.moneyfi.apigateway.repository.UserRepo;
import com.moneyfi.apigateway.model.User;
import com.moneyfi.apigateway.model.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepo repo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = repo.findByUsername(username);

        if (user == null) {
            System.out.println("No user with this User Name: " + username);
            throw new UsernameNotFoundException("User Not Found");
        } else {
            return new UserPrincipal(user);
        }
    }
}
