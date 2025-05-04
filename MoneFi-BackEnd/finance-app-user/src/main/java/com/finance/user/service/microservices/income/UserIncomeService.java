package com.finance.user.service.microservices.income;


import com.finance.user.dto.IncomeModel;

import java.util.List;

public interface UserIncomeService {
    public IncomeModel addIncome(int userId, IncomeModel income);

    public List<IncomeModel> getAllIncomes(int userId);

    public List<IncomeModel> getAllIncomesByDate(int userId, int month, int year);

    public List<IncomeModel> getAllIncomesByYear(int userId, int year);

    public IncomeModel updateIncome(int id, IncomeModel income);

    public boolean deleteIncomeById(int id);
}
