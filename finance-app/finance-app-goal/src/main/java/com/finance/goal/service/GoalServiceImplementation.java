package com.finance.goal.service;

import com.finance.goal.model.GoalModel;
import com.finance.goal.repository.GoalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@Service
public class GoalServiceImplementation implements GoalService{

    @Autowired
    private GoalRepository goalRepository;

    @Override
    public GoalModel save(GoalModel goal) {
        return goalRepository.save(goal);
    }

    @Override
    public GoalModel addAmount(int id, double amount) {
        GoalModel goalModel = goalRepository.findById(id).orElse(null);
        goalModel.setCurrentAmount(goalModel.getCurrentAmount() + amount);
        return save(goalModel);
    }

    @Override
    public List<GoalModel> getAllGoals(int userId) {
        return goalRepository.findByUserId(userId).stream().sorted((a,b)->a.getId()-b.getId()).toList();
    }

    @Override
    public Double getCurrentTotalGoalIncome(int userId) {
        Double totalGoalIncome = goalRepository.getCurrentTotalGoalIncome(userId);
        if(totalGoalIncome == null){
            return 0.0;
        }

        return totalGoalIncome;
    }

    @Override
    public Double getTargetTotalGoalIncome(int userId) {
        Double totalGoalTargetIncome = goalRepository.getTargetTotalGoalIncome(userId);
        if(totalGoalTargetIncome == null){
            return 0.0;
        }

        return totalGoalTargetIncome;
    }

    @Override
    public GoalModel updateByGoalName(int id, GoalModel goal) {
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
    public boolean deleteGoalById(int id) {

        try {
            goalRepository.deleteById(id);
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }
}
