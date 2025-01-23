package com.finance.user.api;

import com.finance.user.dto.BudgetModel;
import com.finance.user.dto.ExpenseModel;
import com.finance.user.dto.GoalModel;
import com.finance.user.dto.IncomeModel;
import com.finance.user.model.ProfileModel;
import com.finance.user.model.UserModel;
import com.finance.user.repository.ProfileRepository;
import com.finance.user.repository.UserRepository;
import com.finance.user.service.UserService;
import com.finance.user.service.microservices.expense.UserExpenseService;
import com.finance.user.service.microservices.goal.UserGoalService;
import com.finance.user.service.microservices.income.UserIncomeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserIncomeService incomeService;

    @Autowired
    private UserExpenseService expenseService;

    @Autowired
    private UserGoalService goalService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ProfileRepository profileRepository;

    //api call to check api gateway
    @GetMapping("/hello")
    public String hello(HttpServletRequest request)
    {
        return "Hello World Page " + request.getSession().getId();
    }

    //call from api gateway to save the user details
    @PostMapping("/setDetails/{userId}/{name}/{email}")
    public UserModel setUserDetails(@PathVariable("userId") int userId, @PathVariable("name") String name, @PathVariable("email") String email){
        UserModel userModel = new UserModel();

        userModel.setUserId(userId);
        userModel.setName(name);
        userModel.setEmail(email);

        ProfileModel profile = new ProfileModel();
        profile.setUserId(userId);
        profile.setName(name);
        profile.setEmail(email);
        ProfileModel addedProfile = profileRepository.save(profile);

        return userRepository.save(userModel);
    }

    @PostMapping
    public ResponseEntity<UserModel> save(@RequestBody UserModel user) {
        UserModel user2 = userService.save(user);
        if (user2 != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(user2);
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }







    // Income Api calls
    // add income
    @PostMapping("/{userId}/income")
    public ResponseEntity<IncomeModel> addIncome(@PathVariable int userId, @RequestBody IncomeModel income) {
        IncomeModel createdIncome = incomeService.addIncome(userId, income);
        if(createdIncome!=null){
            return ResponseEntity.status(HttpStatus.CREATED).body(createdIncome);
        }
        else{
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }
    // get list of incomes of a particular user
    @GetMapping("/{userId}/incomes")
    public ResponseEntity<List<IncomeModel>> getAllIncomes(@PathVariable("userId") int userId) {
        List<IncomeModel> incomesList = incomeService.getAllIncomes(userId);
        return ResponseEntity.ok(incomesList);
    }
    // get total income of a user
//    @GetMapping("/{userId}/totalIncome")
//    public Integer getTotalIncome(@PathVariable("userId") int userId){
//        List<IncomeModel> incomesList = incomeService.getAllIncomes(userId);
//        return (int) incomesList.stream().mapToDouble(i->i.getAmount()).sum();
//    }
    // get total income of a user in a particular month and year
    @GetMapping("/{userId}/totalIncome/{month}/{year}")
    public Double getTotalIncomeByMonthAndYear(@PathVariable("userId") int userId, @PathVariable("month") int month, @PathVariable("year") int year){
        List<IncomeModel> incomesList = incomeService.getAllIncomesByDate(userId, month, year, false);
        return incomesList.stream().mapToDouble(i->i.getAmount()).sum();
    }
    //get remaining balance of a user upto previous month in a year
    @GetMapping("/{userId}/totalRemainingIncomeOfPreviousMonth/{month}/{year}")
    public Integer getTotalRemainingIncomeByMonthAndYear(
            @PathVariable("userId") int userId,
            @PathVariable("month") int month,
            @PathVariable("year") int year) {

        // Adjust month and year to point to the previous month
        final int adjustedMonth;
        final int adjustedYear;

        if (month == 1) { // Handle January case
            adjustedMonth = 12;
            adjustedYear = year - 1;
        } else {
            adjustedMonth = month - 1;
            adjustedYear = year;
        }

        // Fetch incomes up to the previous month
        List<IncomeModel> incomesList = incomeService.getAllIncomes(userId);
        double totalIncome = incomesList.stream()
                .filter(i -> (i.getDate().getYear() < adjustedYear) ||
                        (i.getDate().getYear() == adjustedYear && i.getDate().getMonthValue() <= adjustedMonth))
                .mapToDouble(i -> i.getAmount())
                .sum();

        // Fetch expenses up to the previous month
        List<ExpenseModel> expensesList = expenseService.getAllExpenses(userId);
        double totalExpenses = expensesList.stream()
                .filter(e -> (e.getDate().getYear() < adjustedYear) ||
                        (e.getDate().getYear() == adjustedYear && e.getDate().getMonthValue() <= adjustedMonth))
                .mapToDouble(e -> e.getAmount())
                .sum();

        // Return the remaining amount as an integer
        return (int) (totalIncome - totalExpenses);
    }
    // get list of incomes of a user in a particular month and year
    @GetMapping("/incomes/{userId}/{month}/{year}/{deleteStatus}")
    public ResponseEntity<List<IncomeModel>> getAllIncomesByDate(@PathVariable("userId") int userId,
                                                                   @PathVariable("month") int month,
                                                                   @PathVariable("year") int year,
                                                                   @PathVariable("deleteStatus") boolean deleteStatus) {
        List<IncomeModel> incomesList = incomeService.getAllIncomesByDate(userId, month, year, deleteStatus);
        return ResponseEntity.ok(incomesList);
    }
    // get all incomes of a user in a year
    @GetMapping("/incomes/{userId}/{year}/{deleteStatus}")
    public ResponseEntity<List<IncomeModel>> getAllIncomesByYear(@PathVariable("userId") int userId,
                                                                   @PathVariable("year") int year,
                                                                   @PathVariable("deleteStatus") boolean deleteStatus) {
        List<IncomeModel> incomesList = incomeService.getAllIncomesByYear(userId, year, deleteStatus);
        return ResponseEntity.ok(incomesList);
    }
    // get list of total income of every month in a year
    @GetMapping("/{userId}/monthlyTotalIncomesList/{year}")
    public List<Double> getMonthlyInocmeTotals(@PathVariable("userId") int userId, @PathVariable("year") int year) {
        return incomeService.getAllIncomesOfEveryMonth(userId, year);
    }
    // update a particular income of a user by income id
    @PutMapping("/{id}/income")
    public ResponseEntity<IncomeModel> updateIncome(@PathVariable("id") int id, @RequestBody IncomeModel income){
        IncomeModel updatedIncome = incomeService.updateIncome(id, income);
        if(updatedIncome!=null){
            return ResponseEntity.status(HttpStatus.CREATED).body(updatedIncome);
        }
        else{
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }
    // delete a particular income of a user by income id
    @DeleteMapping("/{id}/income")
    public ResponseEntity<Void> deleteIncomeById(@PathVariable("id") int id) {
        boolean isDeleted = incomeService.deleteIncomeById(id);
        if (isDeleted) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // 204: No Content
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404: Not Found
        }
    }







    // Expense Api calls
    // add expense of a user
    @PostMapping("/{userId}/expense")
    public ResponseEntity<ExpenseModel> addExpense(@PathVariable int userId, @RequestBody ExpenseModel expense) {
        ExpenseModel createdExpense = expenseService.addExpense(userId, expense);
        if(createdExpense!=null){
            return ResponseEntity.status(HttpStatus.CREATED).body(createdExpense);
        }
        else{
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }
    // get all expenses of a user
    @GetMapping("/{userId}/expenses")
    public ResponseEntity<List<ExpenseModel>> getAllExpenses(@PathVariable("userId") int userId) {
        List<ExpenseModel> expensesList = expenseService.getAllExpenses(userId);
        return ResponseEntity.ok(expensesList);
    }
    // get list of expenses of a user in a particular month and year
    @GetMapping("/expenses/{userId}/{month}/{year}/{deleteStatus}")
    public ResponseEntity<List<ExpenseModel>> getAllExpensesByDate(@PathVariable("userId") int userId,
                                                                   @PathVariable("month") int month,
                                                                   @PathVariable("year") int year,
                                                                   @PathVariable("deleteStatus") boolean deleteStatus) {
        List<ExpenseModel> expensesList = expenseService.getAllExpensesByDate(userId, month, year, deleteStatus);
        return ResponseEntity.ok(expensesList);
    }
    // get list of expenses of a user in a particular year
    @GetMapping("/expenses/{userId}/{year}/{deleteStatus}")
    public ResponseEntity<List<ExpenseModel>> getAllExpensesByYear(@PathVariable("userId") int userId,
                                                                   @PathVariable("year") int year,
                                                                   @PathVariable("deleteStatus") boolean deleteStatus) {
        List<ExpenseModel> expensesList = expenseService.getAllExpensesByYear(userId, year, deleteStatus);
        return ResponseEntity.ok(expensesList);
    }
//    @GetMapping("/{userId}/totalExpense")
//    public Integer getTotalExpense(@PathVariable("userId") int userId){
//        List<ExpenseModel> expensesList = expenseService.getAllExpenses(userId);
//        return (int) expensesList.stream().mapToDouble(i->i.getAmount()).sum();
//    }
    // get total expense of a user in a particular month and year
    @GetMapping("/expenses/{userId}/totalExpenses/{month}/{year}")
    public Double getTotalExpenseByMonthAndDate(@PathVariable("userId") int userId,
                                                @PathVariable("month") int month,
                                                @PathVariable("year") int year){

        List<ExpenseModel> expensesList = expenseService.getAllExpensesByDate(userId, month, year, false);
        return expensesList.stream().mapToDouble(i->i.getAmount()).sum();
    }
    // get list of expenses by monthly wise in a year
    @GetMapping("/{userId}/monthlyTotalExpensesList/{year}")
    public List<Double> getMonthlyTotals(@PathVariable("userId") int userId, @PathVariable("year") int year) {
        return expenseService.getMonthlyExpenses(userId, year);
    }
    // get total savings of a user in particular month and year
    @GetMapping("/{userId}/totalSavings/{month}/{year}")
    public Double getTotalSavingsByMonthAndDate(@PathVariable("userId") int userId,
                                                @PathVariable("month") int month,
                                                @PathVariable("year") int year){

        List<IncomeModel> incomesList = incomeService.getAllIncomesByDate(userId, month, year, false);
        Double income = incomesList.stream().mapToDouble(i->i.getAmount()).sum();

        List<ExpenseModel> expensesList = expenseService.getAllExpenses(userId);
        Double expense = expensesList.stream().filter(i->i.getDate().getMonthValue()==month && i.getDate().getYear()==year)
                .mapToDouble(i->i.getAmount()).sum();

        return (income - expense);
    }
    @GetMapping("/{userId}/monthlySavingsInYear/{year}")
    public List<Double> getMonthlySavings(@PathVariable("userId") int userId, @PathVariable("year") int year){
        Double[] incomes = restTemplate.getForObject("http://FINANCE-APP-INCOME/api/income/"+userId+"/monthlyTotalIncomesList/"+year,Double[].class);
        Double[] expenses = restTemplate.getForObject("http://FINANCE-APP-EXPENSE/api/expense/"+userId+"/monthlyTotalExpensesList/"+year,Double[].class);

        List<Double> savings = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            savings.add(incomes[i] - expenses[i]);
        }
        return savings;
    }
    @GetMapping("/{userId}/monthlyCumulativeSavingsInYear/{year}")
    public List<Double> getCumulativeMonthlySavings(@PathVariable("userId") int userId, @PathVariable("year") int year){
        Double[] incomes = restTemplate.getForObject("http://FINANCE-APP-INCOME/api/income/"+userId+"/monthlyTotalIncomesList/"+year,Double[].class);
        Double[] expenses = restTemplate.getForObject("http://FINANCE-APP-EXPENSE/api/expense/"+userId+"/monthlyTotalExpensesList/"+year,Double[].class);

        LocalDate currentDate = LocalDate.now();
        int currentYear = currentDate.getYear();
        int currentMonth = currentDate.getMonthValue();

        if(year > currentYear) return Arrays.asList(new Double[12]);

        int lastMonth = (year < currentYear) ? 12 : currentMonth;

        List<Double> savings = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            if(i < lastMonth){
                savings.add(incomes[i] - expenses[i]);
            }
            else{
                savings.add(0.0);
            }
        }

        List<Double> cumulativeSavings = new ArrayList<>();
        cumulativeSavings.add(savings.get(0));
        for(int i=1; i<12; i++){
            if(i < lastMonth){
                cumulativeSavings.add(cumulativeSavings.get(i-1)+savings.get(i));
            }
            else {
                cumulativeSavings.add(0.0);
            }
        }
        return cumulativeSavings;
    }
    @PutMapping("/{id}/expense")
    public ResponseEntity<ExpenseModel> updateExpense(@PathVariable("id") int id, @RequestBody ExpenseModel expense){
        ExpenseModel updatedExpense = expenseService.updateExpense(id, expense);
        if(updatedExpense!=null){
            return ResponseEntity.status(HttpStatus.CREATED).body(updatedExpense);
        }
        else{
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }
    @DeleteMapping("/{id}/expense")
    public ResponseEntity<Void> deleteExpenseById(@PathVariable("id") int id) {
        boolean isDeleted = expenseService.deleteExpenseById(id);
        if (isDeleted) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // 204: No Content
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404: Not Found
        }
    }










    // Budget calls
    @PostMapping("/{userId}/budget")
    public ResponseEntity<BudgetModel> addBudget(@PathVariable int userId, @RequestBody BudgetModel budget) {
        budget.setUserId(userId);
        BudgetModel createdBudget = restTemplate.postForObject("http://FINANCE-APP-BUDGET/api/budget", budget, BudgetModel.class);
        if(createdBudget!=null){
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBudget);
        }
        else{
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }
    @GetMapping("/{userId}/budgets")
    public ResponseEntity<List<BudgetModel>> getAllBudgets(@PathVariable("userId") int userId) {
        BudgetModel[] list = restTemplate.getForObject("http://FINANCE-APP-BUDGET/api/budget/" + userId, BudgetModel[].class);
        List<BudgetModel> budgetList = new ArrayList<>(Arrays.asList(list));
        if (!budgetList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(budgetList);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    @PutMapping("/{id}/budgets")
    public ResponseEntity<BudgetModel> updateAllBudgets(@PathVariable("id") int id, @RequestBody BudgetModel budgetModel) {
        ResponseEntity<BudgetModel> response = restTemplate.exchange(
                "http://FINANCE-APP-BUDGET/api/budget/" + id,
                HttpMethod.PUT,
                new HttpEntity<>(budgetModel),
                BudgetModel.class
        );

        // Optionally handle the response body or status
        return response;
    }









    // Goal Api calls
    @PostMapping("/{userId}/goal")
    public ResponseEntity<GoalModel> addGoal(@PathVariable int userId, @RequestBody GoalModel goal) {
        GoalModel createdGoal = goalService.addGoal(userId, goal);
        if(createdGoal!=null){
            return ResponseEntity.status(HttpStatus.CREATED).body(createdGoal);
        }
        else{
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }
    @GetMapping("/{userId}/goals")
    public ResponseEntity<List<GoalModel>> getAllGoals(@PathVariable("userId") int userId) {
        List<GoalModel> goalsList = goalService.getAllGoals(userId);
        return ResponseEntity.ok(goalsList);
    }
    @GetMapping("/{userId}/totalCurrentGoalIncome")
    public Integer getCurrentTotalGoalIncome(@PathVariable("userId") int userId){
        List<GoalModel> goalsList = goalService.getAllGoals(userId);
        return (int) goalsList.stream().mapToDouble(i->i.getCurrentAmount()).sum();
    }
    @GetMapping("/{userId}/totalTargetGoalIncome")
    public Integer getTargetTotalGoalIncome(@PathVariable("userId") int userId){
        List<GoalModel> goalsList = goalService.getAllGoals(userId);
        return (int) goalsList.stream().mapToDouble(i->i.getTargetAmount()).sum();
    }
    @PutMapping("/{id}/goal")
    public ResponseEntity<GoalModel> updateGoal(@PathVariable("id") int id, @RequestBody GoalModel goal){
        GoalModel updatedGoalList = goalService.updateGoal(id, goal);
        if(updatedGoalList!=null){
            return ResponseEntity.status(HttpStatus.CREATED).body(updatedGoalList);
        }
        else{
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }
    @DeleteMapping("/{id}/goal")
    public ResponseEntity<Void> deleteGoalById(@PathVariable("id") int id) {
        boolean isDeleted = goalService.deleteGoalById(id);
        if (isDeleted) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // 204: No Content
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404: Not Found
        }
    }
    @PostMapping("/{userId}/addAmount/{id}/{amount}")
    public GoalModel addAmount(@PathVariable("id") int id, @PathVariable("amount") double amount, @PathVariable("userId") int userId){
        GoalModel goalModel = restTemplate.postForObject("http://FINANCE-APP-GOAL/api/goal/"+id+"/addAmount/"+amount, null, GoalModel.class);
        return goalModel;
    }








    

    // frontend overview component calls
    @GetMapping("/{userId}/budgetProgress/{month}/{year}")
    public Double budgetProgress(@PathVariable("userId") int userId, @PathVariable("month") int month, @PathVariable("year") int year){
        BudgetModel[] list = restTemplate.getForObject("http://FINANCE-APP-BUDGET/api/budget/"+userId, BudgetModel[].class);
        List<BudgetModel> budgetsList = new ArrayList<>(Arrays.asList(list));

        List<ExpenseModel> expensesList = expenseService.getAllExpensesByDate(userId, month, year, false);
        double currentSpending = expensesList.stream().mapToDouble(i->i.getAmount()).sum();

        double moneyLimit = budgetsList.stream().mapToDouble(i->i.getMoneyLimit()).sum();
        return currentSpending/moneyLimit;
    }


    // profile component api call
    @PostMapping("/profile/{userId}")
    public ProfileModel saveProfile(@PathVariable("userId") int userId, @RequestBody ProfileModel profile){
        profile.setUserId(userId);
        ProfileModel fetchProfile = profileRepository.findByUserId(userId);
        fetchProfile.setName(profile.getName());
        fetchProfile.setEmail(profile.getEmail());
        fetchProfile.setPhone(profile.getPhone());
        fetchProfile.setAddress(profile.getAddress());
        fetchProfile.setIncomeRange(profile.getIncomeRange());
        fetchProfile.setProfileImage(profile.getProfileImage());

        return profileRepository.save(fetchProfile);
    }
    @GetMapping("/profile/{userId}")
    public ProfileModel getProfile(@PathVariable("userId") int userId){
        return profileRepository.findByUserId(userId);
    }
    @GetMapping("/getName/{userId}")
    public String getNameFromUserId(@PathVariable("userId") int userId){
        return userService.getNameFromUserId(userId);
    }

}




