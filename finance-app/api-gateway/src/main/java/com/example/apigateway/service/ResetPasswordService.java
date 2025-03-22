package com.example.apigateway.service;


import com.example.apigateway.repository.UserRepo;
import com.example.apigateway.model.User;
import com.example.apigateway.util.EmailFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ResetPasswordService {
    @Autowired
    private UserRepo userRepository;

    @Autowired
    private EmailFilter emailUtil;


    public String forgotPassword(String email) {

        User user = userRepository.findByUsername(email);
        if(user==null){
            throw new RuntimeException("No user Found");
        }


        String verificationCode = emailUtil.generateVerificationCode();


        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpiration(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);


        String subject = "Password Reset Verification Code";
        String body = "<html>"
                + "<body>"
                + "<h2 style='color: #333;'>Password Reset Verification</h2>"
                + "<p style='font-size: 16px;'>Hello,</p>"
                + "<p style='font-size: 16px;'>You have requested to reset your password. Please use the following verification code:</p>"
                + "<p style='font-size: 20px; font-weight: bold; color: #007BFF;'>" + verificationCode + "</p>"
                + "<p style='font-size: 16px;'>This code is valid for short duration only. If you did not raise, please ignore this email.</p>"
                + "<hr>"
                + "<p style='font-size: 14px; color: #555;'>If you have any issues, feel free to contact us at bharadwajkodi2003@gmail.com</p>"
                + "<br>"
                + "<p style='font-size: 14px;'>Best regards,</p>"
                + "<p style='font-size: 14px;'>The Support Team</p>"
                + "</body>"
                + "</html>";
        emailUtil.sendEmail(email, subject, body);

        return "Verification code sent to your email!";
    }


    public boolean verifyCode(String email, String code) {
        User user = userRepository.findByUsername(email);

        if(user==null){
             throw  new RuntimeException("User not found");
        }


        if (user.getVerificationCode().equals(code) && LocalDateTime.now().isBefore(user.getVerificationCodeExpiration())) {
            return true;
        }

        return false;
    }
    public String UpdatePassword(String email,String password){
        User user = userRepository.findByUsername(email);
        if(user==null)
        {
            return "user not found for given email...";
        }
        PasswordEncoder passwordEncoder=new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        return "Password updated successfully!...";
    }
}
