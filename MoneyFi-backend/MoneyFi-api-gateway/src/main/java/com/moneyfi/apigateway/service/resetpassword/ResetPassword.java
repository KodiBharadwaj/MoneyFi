package com.moneyfi.apigateway.service.resetpassword;

public interface ResetPassword {

    String forgotPassword(String email);

    boolean verifyCode(String email, String code);

    String UpdatePassword(String email,String password);
}
