package com.finance.user.service;

import com.finance.user.model.UserModel;
import com.finance.user.repository.ProfileRepository;
import com.finance.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Override
    public List<UserModel> getAllUsers() {
        return userRepository.findAll();
    }

//    @Override
//    public UserModel getUserByUsername(String username) {
//        return userRepository.findByUsername(username);
//    }

//    @Override
//    public UserModel updateUser(String username, String password) {
//        UserModel user = userRepository.findByUsername(username);
//        if(user == null){
//            throw new RuntimeException("User doesn't exists!");
//        }
//
//        user.setPassword(password);
//        return user;
//    }

    @Override
    public void deleteUserById(int userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public Integer getUserIdFromEmail(String email) {
        return userRepository.getUserIdFromEmail(email);
    }

    @Override
    public String getNameFromUserId(int userId) {
        return profileRepository.getNameFromUserId(userId);
    }
}
