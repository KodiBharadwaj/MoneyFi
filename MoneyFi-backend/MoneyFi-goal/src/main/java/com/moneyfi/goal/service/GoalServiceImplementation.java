package com.moneyfi.goal.service;

import com.moneyfi.goal.model.GoalModel;
import com.moneyfi.goal.repository.GoalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

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
    public GoalModel addAmount(Long id, double amount) {
        GoalModel goalModel = goalRepository.findById(id).orElse(null);
        goalModel.setCurrentAmount(goalModel.getCurrentAmount() + amount);
        return save(goalModel);
    }

    @Override
    public List<GoalModel> getAllGoals(Long userId) {
        return goalRepository.findByUserId(userId).stream().sorted((a,b)-> Math.toIntExact(a.getId() - b.getId())).toList();
    }

    @Override
    public Double getCurrentTotalGoalIncome(Long userId) {
        Double totalGoalIncome = goalRepository.getCurrentTotalGoalIncome(userId);
        if(totalGoalIncome == null){
            return 0.0;
        }

        return totalGoalIncome;
    }

    @Override
    public Double getTargetTotalGoalIncome(Long userId) {
        Double totalGoalTargetIncome = goalRepository.getTargetTotalGoalIncome(userId);
        if(totalGoalTargetIncome == null){
            return 0.0;
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
        if(goal.getCurrentAmount() > 0){
            goalModel.setCurrentAmount(goal.getCurrentAmount());
        }
        if(goal.getTargetAmount() > 0){
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
