package com.moneyfi.transaction.batch.reader;

import com.moneyfi.transaction.service.transaction.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@StepScope
@RequiredArgsConstructor
public class UserSchedulingNotificationReader implements ItemReader<String> {

    private final TransactionService transactionService;

    private List<String> usernames = Collections.emptyList();
    private int currentIndex = 0;
    private int offset = 0;
    private static final int PAGE_SIZE = 100;

    @Override
    public String read() {
        if(currentIndex >= usernames.size()){
            usernames = transactionService.findAllUsernamesOfUsers(offset, PAGE_SIZE);
            System.out.println("checking usernames: " + usernames);
            offset += PAGE_SIZE;
            currentIndex = 0;
            if(usernames.isEmpty()){
                return null;
            }
        }
        return usernames.get(currentIndex++);
    }
}
