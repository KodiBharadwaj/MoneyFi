package com.moneyfi.apigateway.service.userservice;

import com.moneyfi.apigateway.dto.ChangePasswordDto;
import com.moneyfi.apigateway.dto.ProfileChangePassword;
import com.moneyfi.apigateway.dto.RemainingTimeCountDto;
import com.moneyfi.apigateway.model.User;

public interface UserService {

    User saveUser(User user);

    ProfileChangePassword changePassword(ChangePasswordDto changePasswordDto);

    RemainingTimeCountDto checkOtpActiveMethod(String email);

    boolean sendContactUsEmailOfUser(String message, String images);

    boolean sendUserFeedBackEmail(int rating, String comment);
}
