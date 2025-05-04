package com.finance.user.service.microservices.goal;

import com.finance.user.dto.GoalModel;
import com.finance.user.feign.GoalFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class UserGoalServiceImplementation implements UserGoalService {

    @Autowired
    private GoalFeignClient goalFeignClient;


    @Override
    public GoalModel addGoal(int userId, GoalModel goal) {
        goal.setUserId(userId);
        return goalFeignClient.saveGoal(goal).getBody();
    }

    @Override
    public List<GoalModel> getAllGoals(int userId) {
        return goalFeignClient.getAllGoals(userId).getBody();
    }

    @Override
    public GoalModel updateGoal(int id, GoalModel goal) {
        return goalFeignClient.updateGoal(id, goal).getBody();
    }

    @Override
    public boolean deleteGoalById(int id) {

        try {
            goalFeignClient.deleteGoalById(id);
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }
}
