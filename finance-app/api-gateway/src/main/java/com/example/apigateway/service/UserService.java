package com.example.apigateway.service;

import com.example.apigateway.dto.ChangePasswordDto;
import com.example.apigateway.dto.RemainingTimeCountDto;
import com.example.apigateway.repository.UserRepo;
import com.example.apigateway.model.User;
import com.example.apigateway.util.EmailFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

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

    public RemainingTimeCountDto checkOtpActiveMethod(String email){
        RemainingTimeCountDto remainingTimeCountDto = new RemainingTimeCountDto();

        User user = userRepo.findByUsername(email);
        if(user == null){
            remainingTimeCountDto.setComment("User not exist");
            remainingTimeCountDto.setResult(false);
            return remainingTimeCountDto;
        }

        if(user.getOtpCount() >= 3){
            remainingTimeCountDto.setResult(false);
            remainingTimeCountDto.setComment("Otp limit crossed");
            return remainingTimeCountDto;
        }

        if(user.getVerificationCodeExpiration() == null || user.getVerificationCodeExpiration().isBefore(LocalDateTime.now())){
            remainingTimeCountDto.setResult(true);
            return remainingTimeCountDto;
        }

        LocalDateTime time1 = LocalDateTime.now();
        LocalDateTime time2 = user.getVerificationCodeExpiration();
        long minutesDifference = ChronoUnit.MINUTES.between(time1, time2);
        remainingTimeCountDto.setRemainingMinutes((int) minutesDifference);
        remainingTimeCountDto.setResult(false);
        return remainingTimeCountDto;
    }


    @Scheduled(fixedRate = 3600000) // Runs every 1 hour
    public void removeOtpCountOfPreviousDay() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        List<User> userList = userRepo.getUserListWhoseOtpCountGreaterThanThree();

        for (User user : userList) {
            if (user.getOtpCount() >= 3 && user.getVerificationCodeExpiration().isBefore(startOfToday)) {
                user.setOtpCount(0);
                userRepo.save(user);
            }
        }
    }
}
