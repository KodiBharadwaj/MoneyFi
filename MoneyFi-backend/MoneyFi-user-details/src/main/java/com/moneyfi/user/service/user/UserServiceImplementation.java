package com.moneyfi.user.service.user;

import com.moneyfi.user.model.UserModel;
import com.moneyfi.user.repository.ProfileRepository;
import com.moneyfi.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImplementation implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Override
    public UserModel save(UserModel user) {
        return userRepository.save(user);
    }

}