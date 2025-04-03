package com.moneyfi.apigateway.service.userservice;

import com.moneyfi.apigateway.dto.ChangePasswordDto;
import com.moneyfi.apigateway.dto.RemainingTimeCountDto;
import com.moneyfi.apigateway.model.User;

public interface UserService {

    public User saveUser(User user);

    public boolean changePassword(ChangePasswordDto changePasswordDto);

    public RemainingTimeCountDto checkOtpActiveMethod(String email);
}
