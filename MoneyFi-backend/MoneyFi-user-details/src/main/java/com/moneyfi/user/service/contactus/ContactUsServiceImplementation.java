package com.moneyfi.user.service.contactus;

import com.moneyfi.user.model.ContactUs;
import com.moneyfi.user.model.Feedback;
import com.moneyfi.user.repository.ContactUsRepository;
import com.moneyfi.user.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ContactUsServiceImplementation implements ContactUsService{

    @Autowired
    private RestTemplate restTemplate;

    private final ContactUsRepository contactUsRepository;
    private final FeedbackRepository feedbackRepository;

    public ContactUsServiceImplementation(ContactUsRepository contactUsRepository,
                                          FeedbackRepository feedbackRepository){
        this.contactUsRepository = contactUsRepository;
        this.feedbackRepository = feedbackRepository;
    }

    @Override
    public ContactUs saveContactUsDetails(ContactUs contactUsDetails) {
        new Thread(() -> restTemplate.getForObject("http://MONEYFI-API-GATEWAY/auth/contactUsEmail/" + contactUsDetails.getMessage() + "/" + contactUsDetails.getImages(), Boolean.class)).start();
        return contactUsRepository.save(contactUsDetails);
    }

    @Override
    public Feedback saveFeedback(Feedback feedback) {
        new Thread(() -> restTemplate.getForObject("http://MONEYFI-API-GATEWAY/auth/feedbackEmail/" + feedback.getRating() + "/" + feedback.getComments(), Boolean.class)).start();
        return feedbackRepository.save(feedback);
    }
}
