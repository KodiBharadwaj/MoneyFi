package com.moneyfi.goal.controller;

import com.moneyfi.goal.config.JwtService;
import com.moneyfi.goal.model.GoalModel;
import com.moneyfi.goal.service.GoalService;
import com.moneyfi.goal.service.dto.response.GoalDetailsDto;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/goal")
public class GoalApiController {

    @Autowired
    private RestTemplate restTemplate;

    private final GoalService goalService;
    private final JwtService jwtService;

    public GoalApiController(GoalService goalService,
                             JwtService jwtService){
        this.goalService = goalService;
        this.jwtService = jwtService;
    }

    @GetMapping("/test")
    public Object testFunction(@RequestHeader("Authorization") String authHeader){
        String url = "http://localhost:8200/api/v1/expense/getExpenses";
//        Object object = restTemplate.getForObject(url, Object.class);
//        return object;
        if (authHeader.startsWith("Bearer ")) {
            authHeader = authHeader.substring(7);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authHeader);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.GET, entity, Object.class);
        return response.getBody();
    }

    @Operation(summary = "Method to add a goal")
    @PostMapping("/saveGoal")
    public ResponseEntity<GoalDetailsDto> saveGoal(@RequestBody GoalModel goal,
                                              @RequestHeader("Authorization") String authHeader) {

        BigDecimal amountToBeAdded = BigDecimal.ZERO;
        GoalDetailsDto createdGoal = goalService.save(goal, amountToBeAdded, authHeader);
        if (createdGoal != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(createdGoal); // 201
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null); // 409
        }
    }

    @Operation(summary = "Method to add amount to a particular goal")
    @PostMapping("/{id}/addAmount/{amount}")
    public GoalDetailsDto addAmount(@RequestHeader("Authorization") String authHeader,
                               @PathVariable("id") Long id,
                               @PathVariable("amount") BigDecimal amount){

        return goalService.addAmount(id, amount, authHeader);
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
    public ResponseEntity<GoalDetailsDto> updateGoal(@RequestHeader("Authorization") String authHeader,
                                                @PathVariable("id") Long id,
                                                @RequestBody GoalModel goal) {
        GoalDetailsDto updatedGoal = goalService.updateByGoalName(id, goal, authHeader);

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
        boolean isDeleted = goalService.deleteGoalById(id, authHeader);
        if (isDeleted) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // 204: No Content
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404: Not Found
        }
    }

}
