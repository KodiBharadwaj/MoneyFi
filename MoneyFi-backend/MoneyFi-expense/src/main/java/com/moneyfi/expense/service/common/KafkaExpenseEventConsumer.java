package com.moneyfi.expense.service.common;

import com.moneyfi.expense.service.ExpenseService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class KafkaExpenseEventConsumer {

    private final ExpenseService expenseService;

    public KafkaExpenseEventConsumer(ExpenseService expenseService){
        this.expenseService = expenseService;
    }

    @KafkaListener(topics = "expense-deletion-topic", groupId = "expense-group")
    public void kafkaConsumerToDeleteExpense(String expenseIds) {
        System.out.println("Received expense IDs: " + expenseIds);
        List<Long> expenseIdsList = Arrays.stream(expenseIds.split(","))
                        .map(Long::parseLong)
                        .collect(Collectors.toList());
        expenseService.deleteExpenseById(expenseIdsList);
    }
}
