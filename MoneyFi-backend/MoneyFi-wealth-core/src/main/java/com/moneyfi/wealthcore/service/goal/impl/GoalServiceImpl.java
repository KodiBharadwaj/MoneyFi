package com.moneyfi.wealthcore.service.goal.impl;

import com.moneyfi.wealthcore.config.JwtService;
import com.moneyfi.wealthcore.exceptions.ResourceNotFoundException;
import com.moneyfi.wealthcore.model.goal.GoalModel;
import com.moneyfi.wealthcore.repository.common.CategoryListRepository;
import com.moneyfi.wealthcore.repository.common.WealthCoreRepository;
import com.moneyfi.wealthcore.repository.goal.GoalRepository;
import com.moneyfi.wealthcore.service.goal.GoalService;
import com.moneyfi.wealthcore.service.goal.dto.response.ExpenseModelDto;
import com.moneyfi.wealthcore.service.goal.dto.response.GoalDetailsDto;
import com.moneyfi.wealthcore.service.goal.dto.response.GoalTileDetailsDto;
import com.moneyfi.wealthcore.utils.StringConstants;
import com.moneyfi.wealthcore.utils.enums.CategoryType;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.moneyfi.wealthcore.utils.StringConstants.*;


@Service
@Slf4j
public class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;
    private final WealthCoreRepository wealthCoreRepository;
    private final RestTemplate restTemplate;
    private final JwtService jwtService;
    private final CategoryListRepository categoryListRepository;

    public GoalServiceImpl(GoalRepository goalRepository,
                           WealthCoreRepository wealthCoreRepository,
                           RestTemplate restTemplate,
                           JwtService jwtService,
                           CategoryListRepository categoryListRepository){
        this.goalRepository = goalRepository;
        this.wealthCoreRepository = wealthCoreRepository;
        this.restTemplate = restTemplate;
        this.jwtService = jwtService;
        this.categoryListRepository = categoryListRepository;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
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

    @Override
    @Transactional(rollbackOn = Exception.class)
    public GoalDetailsDto addAmount(Long id, BigDecimal amount, String authHeader) {
        GoalModel goalModel = goalRepository.findById(id).orElse(null);
        if(goalModel == null){
            throw new NullPointerException();
        }
        goalModel.setCurrentAmount(goalModel.getCurrentAmount().add(amount));
        goalModel.setUpdatedAt(CURRENT_DATE_TIME);
        return save(goalModel, amount, authHeader);
    }

    @Override
    public List<GoalDetailsDto> getAllGoals(Long userId) {
        return wealthCoreRepository.getAllGoalsByUserId(userId);
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
        return totalGoalTargetIncome != null ? totalGoalTargetIncome : BigDecimal.ZERO;
    }

    @Override
    public GoalTileDetailsDto getGoalTileDetails(Long userId) {
        GoalTileDetailsDto goalTileDetailsDto = new GoalTileDetailsDto(new HashMap<>());
        goalTileDetailsDto.getGoalTileDetails().put(TOTAL_GOAL_AMOUNT, getCurrentTotalGoalIncome(userId));
        goalTileDetailsDto.getGoalTileDetails().put(TOTAL_GOAL_TARGET_AMOUNT, getTargetTotalGoalIncome(userId));
        goalTileDetailsDto.getGoalTileDetails().put(TOTAL_AVAILABLE_INCOME, goalRepository.getAvailableBalanceOfUser(userId));
        return goalTileDetailsDto;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public ResponseEntity<GoalDetailsDto> updateByGoalName(Long id, GoalModel goal, String authHeader) {
        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserIdFromToken(token);
        goal.setUserId(userId);
        GoalModel goalModel = goalRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        if(goal.getGoalName() != null){
            goalModel.setGoalName(goal.getGoalName());
        }
        if(goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0){
            goalModel.setTargetAmount(goal.getTargetAmount());
        }
        if(goal.getDeadLine() != null){
            goalModel.setDeadLine(goal.getDeadLine());
        }
        if(goal.getDescription() != null){
            goalModel.setDescription(goal.getDescription());
        }
        goalModel.setUpdatedAt(CURRENT_DATE_TIME);
        /**
         * IMPORTANT:
         * Meanwhile, the ssms trigger activates here to update expense row with respective goal data.
         */
        return ResponseEntity.status(HttpStatus.CREATED).body(updatedGoalDtoConversion(goalRepository.save(goalModel)));
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public boolean deleteGoalById(Long id, String authHeader) {
        try {
            GoalModel goalModel = goalRepository.findById(id).orElse(null);
            if (goalModel != null) {
                goalModel.setDeleted(true);
                goalModel.setUpdatedAt(CURRENT_DATE_TIME);
                goalRepository.save(goalModel);
                return functionCallToExpenseServiceToDeleteExpense(goalModel.getExpenseIds(), authHeader);
            }
            return false;
        } catch (HttpClientErrorException.NotFound e) {
            e.printStackTrace();
            return false;
        }
    }

    private Long functionCallToExpenseServiceToSaveExpense(GoalModel goal, BigDecimal amountToBeAdded, String token){
        ExpenseModelDto expenseModelDto = new ExpenseModelDto();
        expenseModelDto.setDescription(goal.getGoalName());
        expenseModelDto.setCategoryId(categoryListRepository.findByType(CategoryType.EXPENSE.name()).stream().filter(category -> category.getCategory().equalsIgnoreCase("Goal")).findFirst().get().getId());
        if(amountToBeAdded.compareTo(BigDecimal.ZERO) == 0){
            expenseModelDto.setAmount(goal.getCurrentAmount());
        } else {
            expenseModelDto.setAmount(amountToBeAdded);
        }
        expenseModelDto.setDate(CURRENT_DATE_TIME);
        expenseModelDto.setRecurring(true);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<ExpenseModelDto> requestEntity = new HttpEntity<>(expenseModelDto, headers);
        ResponseEntity<ExpenseModelDto> response = restTemplate.exchange(
                StringConstants.EUREKA_TRANSACTION_SERVICE_URL + "/expense/saveExpense",
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

    private Boolean functionCallToExpenseServiceToDeleteExpense(String expenseIds, String authHeader){
        String token = authHeader.substring(7);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);
        List<Long> expenseIdsList = Arrays.stream(expenseIds.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
        HttpEntity<List<Long>> requestEntity = new HttpEntity<>(expenseIdsList, headers);
        ResponseEntity<Void> response = restTemplate.exchange(
                StringConstants.EUREKA_TRANSACTION_SERVICE_URL + "/expense",
                HttpMethod.DELETE,
                requestEntity,
                Void.class
        );
        return response.getStatusCode().is2xxSuccessful();
    }

    private GoalDetailsDto updatedGoalDtoConversion(GoalModel updatedGoal){
        GoalDetailsDto goalDetailsDto = new GoalDetailsDto();
        BeanUtils.copyProperties(updatedGoal, goalDetailsDto);
        goalDetailsDto.setDeadLine(Date.valueOf(updatedGoal.getDeadLine().toLocalDate()));
        goalDetailsDto.setCategory(categoryListRepository.findById(updatedGoal.getCategoryId()).get().getCategory());
        return goalDetailsDto;
    }
}
