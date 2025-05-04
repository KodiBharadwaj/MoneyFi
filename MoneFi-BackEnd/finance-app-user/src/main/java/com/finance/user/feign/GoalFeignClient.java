package com.finance.user.feign;


import com.finance.user.dto.GoalModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@FeignClient("FINANCE-APP-GOAL")
public interface GoalFeignClient {

    @PostMapping("/api/goal")
    public ResponseEntity<GoalModel> saveGoal(@RequestBody GoalModel goal);


    @GetMapping("/api/goal/{userId}")
    public ResponseEntity<List<GoalModel>> getAllGoals(@PathVariable("userId") int userId);


    @PutMapping("/api/goal/{id}")
    public ResponseEntity<GoalModel> updateGoal(@PathVariable("id") int id, @RequestBody GoalModel goal);


    @DeleteMapping("/api/goal/{id}")
    public void deleteGoalById(@PathVariable("id") int id);

}
