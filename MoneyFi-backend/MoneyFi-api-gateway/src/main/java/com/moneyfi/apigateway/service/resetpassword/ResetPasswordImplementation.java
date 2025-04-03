package com.moneyfi.apigateway.service.resetpassword;


import com.moneyfi.apigateway.repository.UserRepository;
import com.moneyfi.apigateway.model.User;
import com.moneyfi.apigateway.util.EmailFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
public class ResetPasswordImplementation implements ResetPassword {

    private final UserRepository userRepository;
    private final EmailFilter emailUtil;

    @Autowired
    private RestTemplate restTemplate;

    public ResetPasswordImplementation(EmailFilter emailFilter,
                                       UserRepository userRepository){
        this.emailUtil = emailFilter;
        this.userRepository = userRepository;
    }


    @Override
    public String forgotPassword(String email) {

        User user = userRepository.findByUsername(email);
        if(user==null){
            throw new RuntimeException("No user Found");
        }

        String verificationCode = emailUtil.generateVerificationCode();


        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpiration(LocalDateTime.now().plusMinutes(5));
        user.setOtpCount(user.getOtpCount() + 1);
        userRepository.save(user);

        String userName = restTemplate.getForObject("http://MONEYFI-USER/api/profile/getName/" + user.getId(), String.class);

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
        emailUtil.sendEmail(email, subject, body);

        return "Verification code sent to your email!";
    }

    @Override
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

    @Override
    public String UpdatePassword(String email,String password){
        User user = userRepository.findByUsername(email);
        if(user==null){
            return "user not found for given email...";
        }

        PasswordEncoder passwordEncoder=new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        return "Password updated successfully!...";
    }
}
