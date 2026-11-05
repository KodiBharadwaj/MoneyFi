package com.moneyfi.user.service.secondary;

import com.moneyfi.user.model.secondary.TestMongo;
import com.moneyfi.user.repository.secondary.TestMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TestMongoService {

    private final TestMongoRepository testMongoRepository;

    public void testMongoMethod(String name) {
        testMongoRepository.save(new TestMongo(name));
    }

}
