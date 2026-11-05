package com.moneyfi.user.repository.secondary;

import com.moneyfi.user.model.secondary.TestMongo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestMongoRepository extends MongoRepository<TestMongo, String> {
}
