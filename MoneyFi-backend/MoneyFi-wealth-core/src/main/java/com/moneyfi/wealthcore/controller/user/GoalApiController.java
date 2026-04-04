package com.moneyfi.wealthcore.controller.user;

import com.moneyfi.wealthcore.model.goal.GoalModel;
import com.moneyfi.wealthcore.security.JwtService;
import com.moneyfi.wealthcore.service.goal.GoalService;
import com.moneyfi.wealthcore.service.goal.dto.response.GoalDetailsDto;
import com.moneyfi.wealthcore.service.goal.dto.response.GoalTileDetailsDto;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/wealth-core/goal")
@PreAuthorize("hasRole('USER')")
@Validated
@RequiredArgsConstructor
public class GoalApiController {

    private final GoalService goalService;
    private final JwtService jwtService;

    @Operation(summary = "Api to add a goal")
    @PostMapping("/save")
    public ResponseEntity<Void> saveGoal(@RequestHeader("Authorization") String authHeader,
                                         @Valid @RequestBody GoalModel goal) {
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        goalService.save(goal, userId, authHeader);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Api to add amount to a particular goal")
    @PostMapping("/{id}/addAmount/{amount}")
    public GoalDetailsDto addAmount(@RequestHeader("Authorization") String authHeader,
                                    @NotNull @PathVariable(value = "id") Long id,
                                    @NotNull @PathVariable(value = "amount") BigDecimal amount){
        return goalService.addAmount(id, amount, authHeader);
    }

    @Operation(summary = "Api to get a goal")
    @GetMapping("/getGoalDetails")
    public ResponseEntity<List<GoalDetailsDto>> getAllGoals(@RequestHeader("Authorization") String authHeader) {
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        List<GoalDetailsDto> list = goalService.getAllGoals(userId);
        return ResponseEntity.status(HttpStatus.OK).body(list); // 200
    }

    @Operation(summary = "Api to get total current amount of a particular goal")
    @GetMapping("/totalCurrentGoalIncome")
    public BigDecimal getCurrentTotalGoalIncome(@RequestHeader("Authorization") String authHeader){
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return goalService.getCurrentTotalGoalIncome(userId);
    }

    @Operation(summary = "Api to get total target amount of a particular goal")
    @GetMapping("/totalTargetGoalIncome")
    public BigDecimal getTargetTotalGoalIncome(@RequestHeader("Authorization") String authHeader){
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return goalService.getTargetTotalGoalIncome(userId);
    }

    @Operation(summary = "Api to get goal details tiles in goal ui")
    @GetMapping("/goal-tile-details")
    public GoalTileDetailsDto getGoalTileDetails(@RequestHeader("Authorization") String authHeader){
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return goalService.getGoalTileDetails(userId);
    }

    @Operation(summary = "Api to update goal details")
    @PutMapping("/{id}")
    public ResponseEntity<GoalDetailsDto> updateGoal(@RequestHeader("Authorization") String authHeader,
                                                     @NotNull @PathVariable(value = "id") Long id,
                                                     @RequestBody GoalModel goal) {
        return goalService.updateByGoalName(id, goal, authHeader);
    }

    @Operation(summary = "Api to delete goal by id")
    @DeleteMapping("/{id}")
    public void deleteById(@RequestHeader("Authorization") String authHeader,
                           @NotNull @PathVariable(value = "id") Long id) {
        goalService.deleteGoalById(id, authHeader);
    }
}
