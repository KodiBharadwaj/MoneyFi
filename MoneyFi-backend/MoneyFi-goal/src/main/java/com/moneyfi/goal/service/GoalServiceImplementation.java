package com.moneyfi.goal.service;

import com.moneyfi.goal.model.GoalModel;
import com.moneyfi.goal.repository.GoalRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class GoalServiceImplementation implements GoalService{

    private final GoalRepository goalRepository;

    public GoalServiceImplementation(GoalRepository goalRepository){
        this.goalRepository = goalRepository;
    }

    @Override
    public GoalModel save(GoalModel goal) {
        return goalRepository.save(goal);
    }

    @Override
    public GoalModel addAmount(Long id, BigDecimal amount) {
        GoalModel goalModel = goalRepository.findById(id).orElse(null);
        goalModel.setCurrentAmount(goalModel.getCurrentAmount().add(amount));
        return save(goalModel);
    }

    @Override
    public List<GoalModel> getAllGoals(Long userId) {
        return goalRepository.findByUserId(userId)
                .stream()
                .sorted((a,b)-> Long.compare(a.getId(), b.getId()))
                .toList();
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
        BigDecimal totalGoalTargetIncome = goalRepository.getTargetTotalGoalIncome(userId);
        if(totalGoalTargetIncome == null){
            return BigDecimal.ZERO;
        }

        return totalGoalTargetIncome;
    }

    @Override
    public GoalModel updateByGoalName(Long id, GoalModel goal) {
        GoalModel goalModel = goalRepository.findById(id).orElse(null);

        goal.setUserId(goal.getUserId());

        if(goal.getGoalName() != null){
            goalModel.setGoalName(goal.getGoalName());
        }
        if(goal.getCurrentAmount().compareTo(BigDecimal.ZERO) > 0){
            goalModel.setCurrentAmount(goal.getCurrentAmount());
        }
        if(goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0){
            goalModel.setTargetAmount(goal.getTargetAmount());
        }
        if(goal.getDeadLine() != null){
            goalModel.setDeadLine(goal.getDeadLine());
        }
        if(goal.getCategory() != null){
            goalModel.setCategory(goal.getCategory());
        }

        return save(goalModel);
    }

    @Override
    public boolean deleteGoalById(Long id) {

        try {
            goalRepository.deleteById(id);
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }
}
