package com.moneyfi.apigateway.service.resetpassword;

public interface ResetPassword {

    public String forgotPassword(String email);

    public boolean verifyCode(String email, String code);

    public String UpdatePassword(String email,String password);
}
