package com.moneyfi.user.model.secondary;

import jakarta.persistence.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "test_mongo")
public class TestMongo {
    @Id
    private String id;

    private String name;

    public TestMongo(String name) {
        this.name = name;
    }
}
