package com.moneyfi.apigateway.service.resetpassword;


import com.moneyfi.apigateway.exceptions.ResourceNotFoundException;
import com.moneyfi.apigateway.model.auth.UserAuthModel;
import com.moneyfi.apigateway.repository.auth.UserRepository;
import com.moneyfi.apigateway.repository.common.ProfileRepository;
import com.moneyfi.apigateway.util.EmailFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ResetPasswordImplementation implements ResetPassword {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    public ResetPasswordImplementation(UserRepository userRepository,
                                       ProfileRepository profileRepository){
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
    }


    @Override
    public String forgotPassword(String email) {

        UserAuthModel userAuthModel = userRepository.findByUsername(email);
        if(userAuthModel == null){
            throw new ResourceNotFoundException("No userAuthModel Found");
        }

        String verificationCode = EmailFilter.generateVerificationCode();


        userAuthModel.setVerificationCode(verificationCode);
        userAuthModel.setVerificationCodeExpiration(LocalDateTime.now().plusMinutes(5));
        userAuthModel.setOtpCount(userAuthModel.getOtpCount() + 1);
        userRepository.save(userAuthModel);

        String userName = profileRepository.findByUserId(userAuthModel.getId()).getName();

        String subject = "MoneyFi's Password Reset Verification Code";
        String body = "<html>"
                + "<body>"
                + "<h2 style='color: #333;'>Password Reset Verification</h2>"
                + "<p style='font-size: 16px;'>Hello " + userName + ",</p>"
                + "<p style='font-size: 16px;'>You have requested to reset your password. Please use the following verification code:</p>"
                + "<p style='font-size: 20px; font-weight: bold; color: #007BFF;'>" + verificationCode + "</p>"
                + "<p style='font-size: 16px;'>This code is valid for 5 minutes only. If you did not raise, please ignore this email.</p>"
                + "<hr>"
                + "<p style='font-size: 14px; color: #555;'>If you have any issues, feel free to contact us at bharadwajkodi2003@gmail.com</p>"
                + "<br>"
                + "<p style='font-size: 14px;'>Best regards,</p>"
                + "<p style='font-size: 14px;'>The Support Team</p>"
                + "</body>"
                + "</html>";
        boolean isMailSent = EmailFilter.sendEmail(email, subject, body);

        if(isMailSent){
            return "Verification code sent to your email!";
        }

        return "cant send mail!";

    }

    @Override
    public boolean verifyCode(String email, String code) {
        UserAuthModel userAuthModel = userRepository.findByUsername(email);
        if(userAuthModel == null){
             throw new ResourceNotFoundException("UserAuthModel not found");
        }

        return userAuthModel.getVerificationCode().equals(code) && LocalDateTime.now().isBefore(userAuthModel.getVerificationCodeExpiration());
    }

    @Override
    public String UpdatePassword(String email,String password){
        UserAuthModel userAuthModel = userRepository.findByUsername(email);
        if(userAuthModel ==null){
            return "userAuthModel not found for given email...";
        }

        PasswordEncoder passwordEncoder=new BCryptPasswordEncoder();
        userAuthModel.setPassword(passwordEncoder.encode(password));
        userRepository.save(userAuthModel);
        return "Password updated successfully!...";
    }
}
