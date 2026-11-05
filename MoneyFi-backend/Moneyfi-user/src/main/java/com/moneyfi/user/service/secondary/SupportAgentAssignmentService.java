package com.moneyfi.user.service.secondary;

import com.moneyfi.user.model.secondary.SupportAgentWorkload;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SupportAgentAssignmentService {

    private final MongoTemplate mongoTemplate;

    public SupportAgentWorkload assignAgent() {
        // Compare active workload against each agent's own capacity.
        Query query = new BasicQuery("""
                {
                    "online": true,
                    "available": true,
                    "$expr": {
                        "$lt": [
                            "$activeConversationCount",
                            "$maxConcurrentConversations"
                        ]
                    }
                }
                """);
        query.with(Sort.by(
                Sort.Order.asc("activeConversationCount"),
                Sort.Order.asc("lastAssignedAt"),
                Sort.Order.asc("_id")
        ));

        Update update = new Update()
                .inc("activeConversationCount", 1)
                .set("lastAssignedAt", Instant.now())
                .set("updatedAt", Instant.now());

        return mongoTemplate.findAndModify(
                query,
                update,
                FindAndModifyOptions.options().returnNew(true),
                SupportAgentWorkload.class
        );
    }

    public void releaseAgent(String agentId) {
        Query query = Query.query(
                org.springframework.data.mongodb.core.query.Criteria
                        .where("_id").is(agentId)
                        .and("activeConversationCount").gt(0)
        );

        Update update = new Update()
                .inc("activeConversationCount", -1)
                .set("updatedAt", Instant.now());

        mongoTemplate.updateFirst(
                query,
                update,
                SupportAgentWorkload.class
        );
    }
}
