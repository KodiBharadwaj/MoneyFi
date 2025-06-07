package com.moneyfi.apigateway.service.resetpassword;

import com.moneyfi.apigateway.exceptions.ResourceNotFoundException;
import com.moneyfi.apigateway.model.auth.UserAuthModel;
import com.moneyfi.apigateway.repository.auth.UserRepository;
import com.moneyfi.apigateway.repository.common.ProfileRepository;
import com.moneyfi.apigateway.util.EmailFilter;
import com.moneyfi.apigateway.util.EmailTemplates;
import jakarta.transaction.Transactional;
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
    @Transactional
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

        boolean isMailSent = EmailTemplates.sendOtpForForgotPassword(userName, email, verificationCode);
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
