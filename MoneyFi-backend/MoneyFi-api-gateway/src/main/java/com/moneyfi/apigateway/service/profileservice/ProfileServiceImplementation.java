package com.moneyfi.apigateway.service.profileservice;

import com.moneyfi.apigateway.model.common.ContactUs;
import com.moneyfi.apigateway.model.common.Feedback;
import com.moneyfi.apigateway.model.common.ProfileModel;
import com.moneyfi.apigateway.repository.common.ContactUsRepository;
import com.moneyfi.apigateway.repository.common.FeedbackRepository;
import com.moneyfi.apigateway.repository.common.ProfileRepository;
import com.moneyfi.apigateway.util.EmailFilter;
import org.springframework.stereotype.Service;

@Service
public class ProfileServiceImplementation implements ProfileService{

    private final ProfileRepository profileRepository;
    private final ContactUsRepository contactUsRepository;
    private final FeedbackRepository feedbackRepository;
    private final EmailFilter emailFilter;

    public ProfileServiceImplementation(ProfileRepository profileRepository,
                                        ContactUsRepository contactUsRepository,
                                        FeedbackRepository feedbackRepository,
                                        EmailFilter emailFilter){
        this.profileRepository = profileRepository;
        this.contactUsRepository = contactUsRepository;
        this.feedbackRepository = feedbackRepository;
        this.emailFilter = emailFilter;
    }

    @Override
    public ProfileModel saveUserDetails(Long userId, ProfileModel profile) {
        ProfileModel fetchProfile = profileRepository.findByUserId(userId);

        fetchProfile.setName(profile.getName());
        fetchProfile.setEmail(profile.getEmail());
        fetchProfile.setPhone(profile.getPhone());
        fetchProfile.setGender(profile.getGender());
        fetchProfile.setDateOfBirth(profile.getDateOfBirth());
        fetchProfile.setMaritalStatus(profile.getMaritalStatus());
        fetchProfile.setAddress(profile.getAddress());
        fetchProfile.setIncomeRange(profile.getIncomeRange());
        fetchProfile.setProfileImage(profile.getProfileImage());

        return profileRepository.save(fetchProfile);
    }

    @Override
    public ProfileModel getUserDetailsByUserId(Long userId) {
        return profileRepository.findByUserId(userId);
    }

    @Override
    public String getNameByUserId(Long userId) {
        return profileRepository.findByUserId(userId).getName();
    }

    @Override
    public ContactUs saveContactUsDetails(ContactUs contactUsDetails) {
        new Thread(() -> sendContactAlertMail(contactUsDetails, contactUsDetails.getImages())).start();

        return contactUsRepository.save(contactUsDetails);
    }

    private void sendContactAlertMail(ContactUs contactUsDetails, String images){
        String subject = "MoneyFi's User Report Alert!!";

        String body = "<html>"
                + "<body>"
                + "<p style='font-size: 16px;'>Hello Admin,</p>"
                + "<p style='font-size: 16px;'>You received the Report/defect by a user: </p>"
                + "<br>"
                + "<p style='font-size: 16px;'>" + contactUsDetails.getMessage() + "</p>"
                + "<br>";

        String base64Image = images;
        // If an image is provided, embed it in the email
        if (base64Image != null && !base64Image.isEmpty()) {
            body += "<p><b>Attached Image:</b></p>"
                    + "<img src='" + base64Image + "' width='500px' height='auto'/>";
        }

        body += "</body></html>";

        emailFilter.sendEmail("bharadwajkodi2003@gmail.com", subject, body);
    }

    @Override
    public Feedback saveFeedback(Feedback feedback) {
        new Thread(() -> feedbackAlertMail(feedback)).start();

        return feedbackRepository.save(feedback);
    }

    private void feedbackAlertMail(Feedback feedback){
        String subject = "MoneyFi's User Feedback";

        String body = "<html>"
                + "<body>"
                + "<p style='font-size: 16px;'>Hello Admin,</p>"
                + "<br>"
                + "<p style='font-size: 16px;'> You received feedback: " + feedback.getRating() + "/5 </p>"
                + "<br>"
                + "<p style='font-size: 16px;'>Comment: </p>"
                + "<p style='font-size: 16px;'>" + feedback.getComments() + "</p>";

        emailFilter.sendEmail("bharadwajkodi2003@gmail.com", subject, body);
    }
}
