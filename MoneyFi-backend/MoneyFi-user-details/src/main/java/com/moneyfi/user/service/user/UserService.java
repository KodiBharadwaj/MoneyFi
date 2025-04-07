package com.moneyfi.user.service.user;

import com.moneyfi.user.model.UserDetails;

public interface UserService {

    UserDetails save(UserDetails user);

    UserDetails getUserDetailsByUserId(Long userId);
}