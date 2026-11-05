package com.moneyfi.user.controller.open;

import com.moneyfi.user.service.secondary.TestMongoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mongo")
@RequiredArgsConstructor
public class TestMongoController {

    private final TestMongoService testMongoService;

    @GetMapping
    public void testMongo(@RequestParam String name) {
        testMongoService.testMongoMethod(name);
    }
}
