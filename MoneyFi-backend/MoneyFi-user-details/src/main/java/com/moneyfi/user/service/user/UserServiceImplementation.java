package com.moneyfi.user.service.user;

import com.moneyfi.user.model.UserDetails;
import com.moneyfi.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImplementation implements UserService {

    private final UserRepository userRepository;

    public UserServiceImplementation(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails save(UserDetails user) {
        return userRepository.save(user);
    }

    @Override
    public UserDetails getUserDetailsByUserId(Long userId) {
        return userRepository.findByUserId(userId);
    }

}