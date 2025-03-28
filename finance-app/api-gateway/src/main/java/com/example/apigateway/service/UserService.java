package com.example.apigateway.service;

import com.example.apigateway.dto.ChangePasswordDto;
import com.example.apigateway.repository.UserRepo;
import com.example.apigateway.model.User;
import com.example.apigateway.util.EmailFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    @Autowired
    private UserRepo repo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private EmailFilter emailFilter;


    public User saveUser(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        System.out.println(user.getPassword());
        return repo.save(user);
    }

    public boolean changePassword(ChangePasswordDto changePasswordDto){
        User user = userRepo.findById(changePasswordDto.getUserId()).orElse(null);
        if(user == null) return false;

        if(!encoder.matches(changePasswordDto.getCurrentPassword(), user.getPassword())){
            return false;
        } else {
            user.setPassword(encoder.encode(changePasswordDto.getNewPassword()));
            userRepo.save(user);
        }

//        sendPasswordAlertMail(user.getUsername());
        new Thread(() -> sendPasswordAlertMail(user.getUsername())).start();
        return true;
    }

    public void sendPasswordAlertMail(String email){

        String subject = "Password Change Alert!!";
        String body = "<html>"
                + "<body>"
                + "<p style='font-size: 16px;'>Hello,</p>"
                + "<p style='font-size: 16px;'>You have changed the password for your account with username: " + email + "</p>"
                + "<p style='font-size: 20px; font-weight: bold; color: #007BFF;'> </p>"
                + "<p style='font-size: 16px;'>Kindly Ignore if it by you. If not, reply to this mail immediately to secure account.</p>"
                + "<hr>"
                + "<p style='font-size: 14px; color: #555;'>If you have any issues, feel free to contact us at bharadwajkodi2003@gmail.com</p>"
                + "<br>"
                + "<p style='font-size: 14px;'>Best regards,</p>"
                + "<p style='font-size: 14px;'>The Support Team</p>"
                + "</body>"
                + "</html>";
        emailFilter.sendEmail(email, subject, body);
    }

    public boolean checkOtpActiveMethod(String email){
        User user = userRepo.findByUsername(email);
        if(user == null || user.getOtpCount() > 3){
            return false;
        }

        if(user.getVerificationCodeExpiration() == null || user.getVerificationCodeExpiration().isBefore(LocalDateTime.now())){
            return true;
        }

        return false;
    }
}
