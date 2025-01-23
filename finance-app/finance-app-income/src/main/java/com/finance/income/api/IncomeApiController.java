package com.finance.income.api;

import com.finance.income.model.IncomeModel;
import com.finance.income.service.IncomeService;
import jakarta.ws.rs.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/income")
@CrossOrigin
public class IncomeApiController {

    @Autowired
    private IncomeService incomeService;

    @PostMapping
    public ResponseEntity<IncomeModel> saveIncome(@RequestBody IncomeModel income) {
        IncomeModel income1 = incomeService.save(income);
        if (income1 != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(income1); // 201
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null); // 409
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<IncomeModel>> getAllIncomes(@PathVariable("userId") int userId) {
        List<IncomeModel> list = incomeService.getAllIncomes(userId);
//        if (!list.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(list); // 200
//        } else {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // 404
//        }
    }

    @GetMapping("/{userId}/{month}/{year}/{deleteStatus}")
    public ResponseEntity<List<IncomeModel>> getAllIncomesByDate(@PathVariable("userId") int userId, @PathVariable("month") int month, @PathVariable("year") int year, @PathVariable("deleteStatus") boolean deleteStatus){
        return ResponseEntity.status(HttpStatus.OK).body(incomeService.getAllIncomesByDate(userId, month, year, deleteStatus));
    }

    @GetMapping("/{userId}/{year}/{deleteStatus}")
    public ResponseEntity<List<IncomeModel>> getAllIncomesByYear(@PathVariable("userId") int userId, @PathVariable("year") int year, @PathVariable("deleteStatus") boolean deleteStatus){
        return ResponseEntity.status(HttpStatus.OK).body(incomeService.getAllIncomesByYear(userId, year, deleteStatus));
    }


    @PutMapping("/{id}")
    public ResponseEntity<IncomeModel> updateIncome(@PathVariable("id") int userId, @RequestBody IncomeModel income) {
        IncomeModel updatedIncome = incomeService.updateBySource(userId, income);
        if (updatedIncome != null) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(updatedIncome); // 202
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public void deleteIncomeBySource(@PathVariable("id") int id) {
        incomeService.deleteParticularIncomeBySource(id);
    }

    @GetMapping("/{userId}/monthlyTotalIncomesList/{year}")
    public List<Double> getMonthlyTotals(@PathVariable("userId") int userId, @PathVariable("year") int year) {
        return incomeService.getMonthlyIncomes(userId, year);
    }


}
