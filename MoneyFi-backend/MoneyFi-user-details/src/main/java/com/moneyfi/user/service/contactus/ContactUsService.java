package com.moneyfi.user.service.contactus;

import com.moneyfi.user.model.ContactUs;
import com.moneyfi.user.model.Feedback;

public interface ContactUsService {

    public ContactUs saveContactUsDetails(ContactUs contactUsDetails);

    public Feedback saveFeedback(Feedback feedback);
}
