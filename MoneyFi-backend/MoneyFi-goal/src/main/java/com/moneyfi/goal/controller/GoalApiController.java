package com.moneyfi.goal.controller;

import com.moneyfi.goal.model.GoalModel;
import com.moneyfi.goal.repository.GoalRepository;
import com.moneyfi.goal.service.GoalService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/goal")
public class GoalApiController {

    private final GoalService goalService;
    private final GoalRepository goalRepository;

    public GoalApiController(GoalService goalService,
                             GoalRepository goalRepository){
        this.goalService = goalService;
        this.goalRepository = goalRepository;
    }

    @Operation(summary = "Method to add a goal")
    @PostMapping("/{userId}")
    public ResponseEntity<GoalModel> saveGoal(@RequestBody GoalModel goal,
                                              @PathVariable("userId") Long userId) {
        goal.setUserId((long) userId);
        GoalModel createdGoal = goalService.save(goal);
        if (createdGoal != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(createdGoal); // 201
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null); // 409
        }
    }

    @Operation(summary = "Method to add amount to a particular goal")
    @PostMapping("/{id}/addAmount/{amount}")
    public GoalModel addAmount(@PathVariable("id") Long id,
                               @PathVariable("amount") BigDecimal amount){

        return goalService.addAmount(id, amount);
    }

    @Operation(summary = "Method to get a goal")
    @GetMapping("/{userId}")
    public ResponseEntity<List<GoalModel>> getAllGoals(@PathVariable("userId") Long userId) {
        List<GoalModel> list = goalService.getAllGoals(userId);
        return ResponseEntity.status(HttpStatus.OK).body(list); // 200
    }

    @Operation(summary = "Method to get the total current amount of a particular goal")
    @GetMapping("/{userId}/totalCurrentGoalIncome")
    public BigDecimal getCurrentTotalGoalIncome(@PathVariable("userId") Long userId){
        return goalService.getCurrentTotalGoalIncome(userId);
    }

    @Operation(summary = "Method to get the total target amount of a particular goal")
    @GetMapping("/{userId}/totalTargetGoalIncome")
    public BigDecimal getTargetTotalGoalIncome(@PathVariable("userId") Long userId){
        return goalService.getTargetTotalGoalIncome(userId);
    }

    @Operation(summary = "Method to update the goal details")
    @PutMapping("/{id}")
    public ResponseEntity<GoalModel> updateGoal(@PathVariable("id") Long id,
                                                @RequestBody GoalModel goal) {
        GoalModel updatedGoal = goalService.updateByGoalName(id, goal);

        if(updatedGoal!=null){
            return ResponseEntity.status(HttpStatus.CREATED).body(updatedGoal);
        }
        else{
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }

    @Operation(summary = "Method to delete the goal by goal id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable("id") Long id) {
        boolean isDeleted = goalService.deleteGoalById(id);
        if (isDeleted) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // 204: No Content
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404: Not Found
        }
    }

}
