package com.moneyfi.goal.service.impl;

import com.moneyfi.goal.config.JwtService;
import com.moneyfi.goal.service.dto.response.ExpenseModelDto;
import com.moneyfi.goal.exceptions.ResourceNotFoundException;
import com.moneyfi.goal.model.GoalModel;
import com.moneyfi.goal.repository.GoalRepository;
import com.moneyfi.goal.repository.common.GoalCommonRepository;
import com.moneyfi.goal.service.GoalService;
import com.moneyfi.goal.service.dto.response.GoalDetailsDto;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;
    private final GoalCommonRepository goalCommonRepository;
    private final RestTemplate restTemplate;
    private final JwtService jwtService;

    public GoalServiceImpl(GoalRepository goalRepository,
                           GoalCommonRepository goalCommonRepository,
                           RestTemplate restTemplate,
                           JwtService jwtService){
        this.goalRepository = goalRepository;
        this.goalCommonRepository = goalCommonRepository;
        this.restTemplate = restTemplate;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public GoalDetailsDto save(GoalModel goal, BigDecimal amountToBeAdded, String authHeader) {
        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserIdFromToken(token);
        goal.setUserId(userId);

        Long expenseId = functionCallToExpenseServiceToSaveExpense(goal, amountToBeAdded, token);

        if (goal.getExpenseIds() == null || goal.getExpenseIds().isEmpty()) {
            goal.setExpenseIds(expenseId.toString());
        } else {
            goal.setExpenseIds(goal.getExpenseIds() + "," + expenseId.toString());
        }

        return updatedGoalDtoConversion(goalRepository.save(goal));
    }
    private Long functionCallToExpenseServiceToSaveExpense(GoalModel goal, BigDecimal amountToBeAdded, String token){
        ExpenseModelDto expenseModelDto = new ExpenseModelDto();
        expenseModelDto.setDescription(goal.getGoalName());
        expenseModelDto.setCategory("Goal");
        if(amountToBeAdded == BigDecimal.ZERO){
            expenseModelDto.setAmount(goal.getCurrentAmount());
        } else {
            expenseModelDto.setAmount(amountToBeAdded);
        }
        expenseModelDto.setDate(LocalDate.now());
        expenseModelDto.setRecurring(true);

        String url = "http://localhost:8200/api/v1/expense/saveExpense";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<ExpenseModelDto> requestEntity = new HttpEntity<>(expenseModelDto, headers);

        ResponseEntity<ExpenseModelDto> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                ExpenseModelDto.class
        );
        ExpenseModelDto responseBody = response.getBody();
        if(responseBody == null){
            throw new ResourceNotFoundException("Failed to fetch the expense model");
        }
        return responseBody.getId();
    }

    @Override
    public GoalDetailsDto addAmount(Long id, BigDecimal amount, String authHeader) {
        GoalModel goalModel = goalRepository.findById(id).orElse(null);
        if(goalModel == null){
            throw new NullPointerException();
        }
        goalModel.setCurrentAmount(goalModel.getCurrentAmount().add(amount));
        return save(goalModel, amount, authHeader);
    }

    @Override
    public List<GoalDetailsDto> getAllGoals(Long userId) {
        return goalCommonRepository.getAllGoalsByUserId(userId);
    }

    @Override
    public BigDecimal getCurrentTotalGoalIncome(Long userId) {
        BigDecimal totalGoalIncome = goalRepository.getCurrentTotalGoalIncome(userId);
        if(totalGoalIncome == null){
            return BigDecimal.ZERO;
        }

        return totalGoalIncome;
    }

    @Override
    public BigDecimal getTargetTotalGoalIncome(Long userId) {
        BigDecimal totalGoalTargetIncome = goalRepository.getTotalTargetGoalIncome(userId);
        if(totalGoalTargetIncome == null){
            return BigDecimal.ZERO;
        }

        return totalGoalTargetIncome;
    }

    @Override
    public ResponseEntity<GoalDetailsDto> updateByGoalName(Long id, GoalModel goal, String authHeader) {
        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserIdFromToken(token);

        goal.setUserId(userId);
        GoalModel goalModel = goalRepository.findById(id).orElse(null);
        if(goalModel == null || !goalModel.getUserId().equals(userId)){
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        if(goal.getGoalName() != null){
            goalModel.setGoalName(goal.getGoalName());
        }
        if(goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0){
            goalModel.setTargetAmount(goal.getTargetAmount());
        }
        if(goal.getDeadLine() != null){
            goalModel.setDeadLine(goal.getDeadLine());
        }

        /**
         * IMPORTANT:
         * Meanwhile, the ssms trigger activates here to update expense row with respective goal data.
         */

        return ResponseEntity.status(HttpStatus.CREATED).body(updatedGoalDtoConversion(goalRepository.save(goalModel)));
    }
    private GoalDetailsDto updatedGoalDtoConversion(GoalModel updatedGoal){
        GoalDetailsDto goalDetailsDto = new GoalDetailsDto();
        BeanUtils.copyProperties(updatedGoal, goalDetailsDto);
        goalDetailsDto.setDeadLine(Date.valueOf(updatedGoal.getDeadLine()));
        return goalDetailsDto;
    }

    @Override
    @Transactional
    public boolean deleteGoalById(Long id, String authHeader) {

        try {
            GoalModel goalModel = goalRepository.findById(id).orElse(null);
            if(goalModel != null){
                goalModel.setDeleted(true);
                goalRepository.save(goalModel);

                if(functionCallToExpenseServiceToDeleteExpense(goalModel.getExpenseIds(), authHeader)){
                    return true;
                }
                else return false;
            }
            return false;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }
    private Boolean functionCallToExpenseServiceToDeleteExpense(String expenseIds, String authHeader){
        String token = authHeader.substring(7);
        String url = "http://localhost:8200/api/v1/expense";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);

        List<Long> expenseIdsList = Arrays.stream(expenseIds.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());

        HttpEntity<List<Long>> requestEntity = new HttpEntity<>(expenseIdsList, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                requestEntity,
                Void.class
        );

        return response.getStatusCode().is2xxSuccessful();
    }
}
