package com.finance.user.feign;


import com.finance.user.dto.IncomeModel;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient("FINANCE-APP-INCOME")
public interface IncomeFeignClient {

    @PostMapping("api/income")
    public ResponseEntity<IncomeModel> saveIncome(@RequestBody IncomeModel income);

    @GetMapping("api/income/{userId}")
    public ResponseEntity<List<IncomeModel>> getAllIncomes(@PathVariable("userId") int userId);

    @GetMapping("/api/income/{userId}/{month}/{year}")
    public ResponseEntity<List<IncomeModel>> getAllIncomesByDate(@PathVariable("userId") int userId, @PathVariable("month") int month, @PathVariable("year") int year);

    @GetMapping("/api/income/{userId}/{year}")
    public ResponseEntity<List<IncomeModel>> getAllIncomesByYear(@PathVariable("userId") int userId, @PathVariable("year") int year);

    @PutMapping("api/income/{id}")
    public IncomeModel updateIncome(@PathVariable("id") int id, @RequestBody IncomeModel income);

    @DeleteMapping("api/income/{id}")
    public void deleteIncomeById(@PathVariable("id") int id);
}
