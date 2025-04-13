package com.moneyfi.apigateway.service.userservice;

import com.moneyfi.apigateway.dto.ChangePasswordDto;
import com.moneyfi.apigateway.dto.ProfileChangePassword;
import com.moneyfi.apigateway.dto.RemainingTimeCountDto;
import com.moneyfi.apigateway.model.UserAuthModel;

public interface UserService {

    UserAuthModel saveUser(UserAuthModel userAuthModel);

    ProfileChangePassword changePassword(ChangePasswordDto changePasswordDto);

    RemainingTimeCountDto checkOtpActiveMethod(String email);
}
