package com.moneyfi.income.service.auth;

import com.moneyfi.income.model.auth.UserAuthModel;
import com.moneyfi.income.model.auth.UserPrincipal;
import com.moneyfi.income.repository.auth.UserRepository;
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
