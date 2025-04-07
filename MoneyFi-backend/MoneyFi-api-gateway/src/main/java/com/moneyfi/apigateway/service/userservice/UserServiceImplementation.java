package com.moneyfi.apigateway.service.userservice;

import com.moneyfi.apigateway.dto.ChangePasswordDto;
import com.moneyfi.apigateway.dto.ProfileChangePassword;
import com.moneyfi.apigateway.dto.RemainingTimeCountDto;
import com.moneyfi.apigateway.model.UserAuthModel;
import com.moneyfi.apigateway.repository.UserRepository;
import com.moneyfi.apigateway.util.EmailFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class UserServiceImplementation implements UserService {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    private final UserRepository userRepository;
    private final EmailFilter emailFilter;

    @Autowired
    private RestTemplate restTemplate;

    public UserServiceImplementation(UserRepository userRepository,
                                     EmailFilter emailFilter){
        this.userRepository = userRepository;
        this.emailFilter = emailFilter;
    }

    @Override
    public UserAuthModel saveUser(UserAuthModel userAuthModel) {
        userAuthModel.setPassword(encoder.encode(userAuthModel.getPassword()));
        return userRepository.save(userAuthModel);
    }

    @Override
    public ProfileChangePassword changePassword(ChangePasswordDto changePasswordDto){
        UserAuthModel userAuthModel = userRepository.findById(Long.valueOf(changePasswordDto.getUserId())).orElse(null);

        ProfileChangePassword dto = new ProfileChangePassword();
        if(userAuthModel == null) {
            dto.setFlag(false);
            return dto;
        }

        if(!encoder.matches(changePasswordDto.getCurrentPassword(), userAuthModel.getPassword())){
            dto.setFlag(false);
            return dto;
        }
        else if(userAuthModel.getOtpCount() >= 3){
            dto.setOtpCount(userAuthModel.getOtpCount());
            dto.setFlag(false);
            return dto;
        }

        new Thread(() -> sendPasswordAlertMail(Math.toIntExact(userAuthModel.getId()), userAuthModel.getUsername())).start();

        userAuthModel.setPassword(encoder.encode(changePasswordDto.getNewPassword()));
        userAuthModel.setVerificationCodeExpiration(LocalDateTime.now().plusMinutes(5));
        userAuthModel.setOtpCount(userAuthModel.getOtpCount()+1);
        userRepository.save(userAuthModel);

        dto.setFlag(true);
        return dto;
    }

    private void sendPasswordAlertMail(int userId, String email){

        String userName = restTemplate.getForObject("http://MONEYFI-USER/api/profile/getName/" + userId, String.class);

        String subject = "Password Change Alert!!";
        String body = "<html>"
                + "<body>"
                + "<p style='font-size: 16px;'>Hello " + userName +",</p>"
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

    @Override
    public RemainingTimeCountDto checkOtpActiveMethod(String email){
        RemainingTimeCountDto remainingTimeCountDto = new RemainingTimeCountDto();

        UserAuthModel userAuthModel = userRepository.findByUsername(email);
        if(userAuthModel == null){
            remainingTimeCountDto.setComment("UserAuthModel not exist");
            remainingTimeCountDto.setResult(false);
            return remainingTimeCountDto;
        }

        if(userAuthModel.getOtpCount() >= 3){
            remainingTimeCountDto.setResult(false);
            remainingTimeCountDto.setComment("Otp limit crossed");
            return remainingTimeCountDto;
        }

        if(userAuthModel.getVerificationCodeExpiration() == null || userAuthModel.getVerificationCodeExpiration().isBefore(LocalDateTime.now())){
            remainingTimeCountDto.setResult(true);
            return remainingTimeCountDto;
        }

        LocalDateTime time1 = LocalDateTime.now();
        LocalDateTime time2 = userAuthModel.getVerificationCodeExpiration();
        long minutesDifference = ChronoUnit.MINUTES.between(time1, time2);
        remainingTimeCountDto.setRemainingMinutes((int) minutesDifference);
        remainingTimeCountDto.setResult(false);
        return remainingTimeCountDto;
    }

    @Override
    public boolean sendContactUsEmailOfUser(String message, String base64Image) {

        String subject = "MoneyFi's Report Alert!!";

        String body = "<html>"
                + "<body>"
                + "<p style='font-size: 16px;'>Hello Admin,</p>"
                + "<p style='font-size: 16px;'>You received the Report/defect by a user: </p>"
                + "<br>"
                + "<p style='font-size: 16px;'>" + message + "</p>"
                + "<br>";

        // If an image is provided, embed it in the email
        if (base64Image != null && !base64Image.isEmpty()) {
            body += "<p><b>Attached Image:</b></p>"
                    + "<img src='data:image/png;base64," + base64Image + "' width='500px' height='auto'/>";
        }

        body += "</body></html>";

        emailFilter.sendEmail("bharadwajkodi2003@gmail.com", subject, body);
        return true;
    }

    @Override
    public boolean sendUserFeedBackEmail(int rating, String comment) {
        String subject = "MoneyFi's UserAuthModel Feedback";

        String body = "<html>"
                + "<body>"
                + "<p style='font-size: 16px;'>Hello Admin,</p>"
                + "<p style='font-size: 16px;'>You received feedback of a user: </p>"
                + "<br>"
                + "<p style='font-size: 16px;'>" + rating + "</p>"
                + "<br>"
                + "<p style='font-size: 16px;'>" + comment + "</p>";

        emailFilter.sendEmail("bharadwajkodi2003@gmail.com", subject, body);
        return true;
    }


    @Scheduled(fixedRate = 3600000) // Runs every 1 hour
    public void removeOtpCountOfPreviousDay() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        List<UserAuthModel> userAuthModelList = userRepository.getUserListWhoseOtpCountGreaterThanThree();

        for (UserAuthModel userAuthModel : userAuthModelList) {
            if (userAuthModel.getOtpCount() >= 3 && userAuthModel.getVerificationCodeExpiration().isBefore(startOfToday)) {
                userAuthModel.setOtpCount(0);
                userRepository.save(userAuthModel);
            }
        }
    }
}
