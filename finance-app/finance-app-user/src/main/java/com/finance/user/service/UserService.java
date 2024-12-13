package com.finance.user.service;

import com.finance.user.model.UserModel;

import java.util.List;

public interface UserService {

    public UserModel save(UserModel user);

    public List<UserModel> getAllUsers();

//    public UserModel getUserByUsername(String username);

//    public UserModel updateUser(String username, String password);

    public void deleteUserById(int userId);

    public Integer getUserIdFromEmail(String email);

    public String getNameFromUserId(int userId);

}
