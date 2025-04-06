package com.moneyfi.user.service.user;

import com.moneyfi.user.model.UserModel;

public interface UserService {

    UserModel save(UserModel user);

    UserModel getUserDetailsByUserId(Long userId);
}