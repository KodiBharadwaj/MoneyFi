package com.example.apigateway.service;

import com.example.apigateway.dto.ChangePasswordDto;
import com.example.apigateway.repository.UserRepo;
import com.example.apigateway.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepo repo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Autowired
    private UserRepo userRepo;


    public User saveUser(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        System.out.println(user.getPassword());
        return repo.save(user);
    }

    public boolean changePassword(ChangePasswordDto changePasswordDto){
        User user = userRepo.findById(changePasswordDto.getUserId()).orElse(null);
        if(user == null) return false;

        if(!encoder.matches(changePasswordDto.getCurrentPassword(), user.getPassword())){
            return false;
        } else {
            user.setPassword(encoder.encode(changePasswordDto.getNewPassword()));
            userRepo.save(user);
        }

        return true;
    }
}
