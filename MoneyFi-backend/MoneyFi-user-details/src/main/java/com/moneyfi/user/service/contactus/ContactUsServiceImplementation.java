package com.moneyfi.user.service.contactus;

import com.moneyfi.user.model.ContactUs;
import com.moneyfi.user.model.Feedback;
import com.moneyfi.user.repository.ContactUsRepository;
import com.moneyfi.user.repository.FeedbackRepository;
import com.moneyfi.user.util.EmailFilter;
import org.springframework.stereotype.Service;

@Service
public class ContactUsServiceImplementation implements ContactUsService{

    private final ContactUsRepository contactUsRepository;
    private final FeedbackRepository feedbackRepository;
    private final EmailFilter emailFilter;

    public ContactUsServiceImplementation(ContactUsRepository contactUsRepository,
                                          FeedbackRepository feedbackRepository,
                                          EmailFilter emailFilter){
        this.contactUsRepository = contactUsRepository;
        this.feedbackRepository = feedbackRepository;
        this.emailFilter = emailFilter;
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
