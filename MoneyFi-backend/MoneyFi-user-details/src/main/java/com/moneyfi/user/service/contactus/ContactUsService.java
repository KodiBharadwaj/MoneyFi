package com.moneyfi.user.service.contactus;

import com.moneyfi.user.model.ContactUs;
import com.moneyfi.user.model.Feedback;

public interface ContactUsService {

    ContactUs saveContactUsDetails(ContactUs contactUsDetails);

    Feedback saveFeedback(Feedback feedback);
}
