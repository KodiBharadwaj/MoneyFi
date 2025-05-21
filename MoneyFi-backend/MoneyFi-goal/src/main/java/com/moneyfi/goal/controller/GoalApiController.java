package com.moneyfi.goal.controller;

import com.moneyfi.goal.config.JwtService;
import com.moneyfi.goal.model.GoalModel;
import com.moneyfi.goal.repository.GoalRepository;
import com.moneyfi.goal.service.GoalService;
import com.moneyfi.goal.service.dto.response.GoalDetailsDto;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/goal")
public class GoalApiController {

    private final GoalService goalService;
    private final GoalRepository goalRepository;
    private final JwtService jwtService;

    public GoalApiController(GoalService goalService,
                             GoalRepository goalRepository,
                             JwtService jwtService){
        this.goalService = goalService;
        this.goalRepository = goalRepository;
        this.jwtService = jwtService;
    }

    @Operation(summary = "Method to add a goal")
    @PostMapping("/saveGoal")
    public ResponseEntity<GoalModel> saveGoal(@RequestBody GoalModel goal,
                                              @RequestHeader("Authorization") String authHeader) {
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        goal.setUserId(userId);
        GoalModel createdGoal = goalService.save(goal);
        if (createdGoal != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(createdGoal); // 201
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null); // 409
        }
    }

    @Operation(summary = "Method to add amount to a particular goal")
    @PostMapping("/{id}/addAmount/{amount}")
    public GoalModel addAmount(@RequestHeader("Authorization") String authHeader,
                               @PathVariable("id") Long id,
                               @PathVariable("amount") BigDecimal amount){

        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return goalService.addAmount(id, amount);
    }

    @Operation(summary = "Method to get a goal")
    @GetMapping("/getGoalDetails")
    public ResponseEntity<List<GoalDetailsDto>> getAllGoals(@RequestHeader("Authorization") String authHeader) {
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        List<GoalDetailsDto> list = goalService.getAllGoals(userId);
        return ResponseEntity.status(HttpStatus.OK).body(list); // 200
    }

    @Operation(summary = "Method to get the total current amount of a particular goal")
    @GetMapping("/totalCurrentGoalIncome")
    public BigDecimal getCurrentTotalGoalIncome(@RequestHeader("Authorization") String authHeader){
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return goalService.getCurrentTotalGoalIncome(userId);
    }

    @Operation(summary = "Method to get the total target amount of a particular goal")
    @GetMapping("/totalTargetGoalIncome")
    public BigDecimal getTargetTotalGoalIncome(@RequestHeader("Authorization") String authHeader){
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return goalService.getTargetTotalGoalIncome(userId);
    }

    @Operation(summary = "Method to update the goal details")
    @PutMapping("/{id}")
    public ResponseEntity<GoalModel> updateGoal(@RequestHeader("Authorization") String authHeader,
                                                @PathVariable("id") Long id,
                                                @RequestBody GoalModel goal) {
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        GoalModel updatedGoal = goalService.updateByGoalName(id, userId, goal);

        if(updatedGoal != null){
            return ResponseEntity.status(HttpStatus.CREATED).body(updatedGoal);
        }
        else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }

    @Operation(summary = "Method to delete the goal by goal id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@RequestHeader("Authorization") String authHeader,
                                           @PathVariable("id") Long id) {
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        boolean isDeleted = goalService.deleteGoalById(id);
        if (isDeleted) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // 204: No Content
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404: Not Found
        }
    }

}
