package com.moneyfi.user.controller;

import com.moneyfi.user.model.ContactUs;
import com.moneyfi.user.model.Feedback;
import com.moneyfi.user.service.contactus.ContactUsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
public class ContactUsApiController {

    private final ContactUsService contactUsService;

    public ContactUsApiController(ContactUsService contactUsService){
        this.contactUsService = contactUsService;
    }

    @PostMapping("/{userId}")
    public ContactUs saveContactUsDetails(@PathVariable("userId") Long userId,
                                          @RequestBody ContactUs contactUsDetails){
        contactUsDetails.setUserId(userId);
        return contactUsService.saveContactUsDetails(contactUsDetails);
    }

    @PostMapping("/feedback/{userId}")
    public Feedback saveFeedback(@PathVariable("userId") Long userId,
                                 @RequestBody Feedback feedback){
        feedback.setUserId(userId);
        return contactUsService.saveFeedback(feedback);
    }
}
