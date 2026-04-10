package com.moneyfi.wealthcore.service.goal.impl;

import com.moneyfi.constants.dto.GoalExpenseRelationRequestDto;
import com.moneyfi.wealthcore.exceptions.ResourceNotFoundException;
import com.moneyfi.wealthcore.model.goal.GoalModel;
import com.moneyfi.wealthcore.repository.common.CategoryListRepository;
import com.moneyfi.wealthcore.repository.common.WealthCoreRepository;
import com.moneyfi.wealthcore.repository.goal.GoalRepository;
import com.moneyfi.wealthcore.security.JwtService;
import com.moneyfi.wealthcore.service.api.ExternalApiCallService;
import com.moneyfi.wealthcore.service.common.CommonService;
import com.moneyfi.wealthcore.service.goal.GoalService;
import com.moneyfi.wealthcore.service.goal.dto.response.GoalDetailsDto;
import com.moneyfi.wealthcore.service.goal.dto.response.GoalTileDetailsDto;
import com.moneyfi.wealthcore.utils.enums.CategoryType;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.moneyfi.wealthcore.utils.constants.StringConstants.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;
    private final WealthCoreRepository wealthCoreRepository;
    private final JwtService jwtService;
    private final CategoryListRepository categoryListRepository;
    private final CommonService commonService;
    private final ExternalApiCallService externalApiCallService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GoalDetailsDto save(GoalModel goal, Long userId, String authHeader) {
        String token = authHeader.substring(7);
        goal.setUserId(userId);
        goal.setRecurringAmount(goal.getCurrentAmount());

        GoalModel savedGoal = goalRepository.save(goal);
        functionCallToTransactionServiceToSaveExpense(savedGoal, BigDecimal.ZERO, token);
        return updatedGoalDtoConversion(savedGoal);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GoalDetailsDto addAmount(Long id, BigDecimal amount, String authHeader) {
//        GoalModel goalModel = goalRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(GOAL_NOT_FOUND));
//        goalModel.setCurrentAmount(goalModel.getCurrentAmount().add(amount));
//        goalModel.setUpdatedAt(LocalDateTime.now());
//        return save(goalModel, amount, authHeader);
        return null;
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
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<GoalDetailsDto> updateByGoalName(Long id, GoalModel goal, String authHeader) {
        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserIdFromToken(token);
        goal.setUserId(userId);
        GoalModel goalModel = goalRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(GOAL_NOT_FOUND));

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
        goalModel.setUpdatedAt(LocalDateTime.now());
        /**
         * IMPORTANT:
         * Meanwhile, the ssms trigger activates here to update expense row with respective goal data.
         */
        return ResponseEntity.status(HttpStatus.CREATED).body(updatedGoalDtoConversion(goalRepository.save(goalModel)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGoalById(Long id, String authHeader) {
//        try {
//            GoalModel goalModel = goalRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(GOAL_NOT_FOUND));
//            goalModel.setDeleted(Boolean.TRUE);
//            goalModel.setUpdatedAt(LocalDateTime.now());
//            goalRepository.save(goalModel);
//            functionCallToExpenseServiceToDeleteExpense(goalModel.getExpenseIds(), authHeader);
//        } catch (HttpClientErrorException.NotFound e) {
//            e.printStackTrace();
//        }
    }

    private void functionCallToTransactionServiceToSaveExpense(GoalModel goal, BigDecimal amountToBeAdded, String token){
        GoalExpenseRelationRequestDto goalExpenseRelationRequestDto = new GoalExpenseRelationRequestDto();
        goalExpenseRelationRequestDto.setDescription(goal.getGoalName());
        goalExpenseRelationRequestDto.setCategoryId(commonService.getCategoryWiseList(List.of(CategoryType.EXPENSE.name())).stream().filter(category -> category.getCategory().equalsIgnoreCase("Goal")).findFirst().get().getCategoryId());
        if(amountToBeAdded.compareTo(BigDecimal.ZERO) == 0){
            goalExpenseRelationRequestDto.setAmount(goal.getCurrentAmount());
        } else {
            goalExpenseRelationRequestDto.setAmount(amountToBeAdded);
        }
        goalExpenseRelationRequestDto.setDate(LocalDateTime.now());
        goalExpenseRelationRequestDto.setRecurring(Boolean.TRUE);
        goalExpenseRelationRequestDto.setGoalId(goal.getId());
        externalApiCallService.externalApiCallToTransactionServiceToSaveExpense(token, "/expense/goal-expense-relation/add", goalExpenseRelationRequestDto);
    }

    private void functionCallToExpenseServiceToDeleteExpense(String expenseIds, String authHeader){
        String token = authHeader.substring(7);
        List<Long> expenseIdsList = Arrays.stream(expenseIds.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
        externalApiCallService.externalApiCallToTransactionServiceToDeleteExpense(token, "/expense", expenseIdsList);
    }

    private GoalDetailsDto updatedGoalDtoConversion(GoalModel updatedGoal){
        GoalDetailsDto goalDetailsDto = new GoalDetailsDto();
        BeanUtils.copyProperties(updatedGoal, goalDetailsDto);
        goalDetailsDto.setDeadLine(Date.valueOf(updatedGoal.getDeadLine().toLocalDate()));
        goalDetailsDto.setCategory(categoryListRepository.findById(updatedGoal.getCategoryId()).orElseThrow(() -> new ResourceNotFoundException("Category not found")).getCategory());
        return goalDetailsDto;
    }
}
