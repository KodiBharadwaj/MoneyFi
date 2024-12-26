package com.finance.user.service.microservices.income;

import com.finance.user.dto.IncomeModel;
import com.finance.user.feign.IncomeFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class UserIncomeServiceImplementation implements UserIncomeService {

    @Autowired
    private IncomeFeignClient incomeFeignClient;


    @Override
    public IncomeModel addIncome(int userId, IncomeModel income) {
        income.setUserId(userId);
        return incomeFeignClient.saveIncome(income).getBody();
    }

    @Override
    public List<IncomeModel> getAllIncomes(int userId) {
        return incomeFeignClient.getAllIncomes(userId).getBody();
    }

    @Override
    public List<IncomeModel> getAllIncomesByDate(int userId, int month, int year) {
        return incomeFeignClient.getAllIncomesByDate(userId, month, year).getBody();
    }

    @Override
    public List<IncomeModel> getAllIncomesByYear(int userId, int year) {
        return incomeFeignClient.getAllIncomesByYear(userId, year).getBody();
    }

    @Override
    public List<Double> getAllIncomesOfEveryMonth(int userId, int year) {
        return incomeFeignClient.getAllIncomesOfEveryMonth(userId, year);
    }

    @Override
    public IncomeModel updateIncome(int id, IncomeModel income) {
//        income.setUserId(income.getUserId());
        return incomeFeignClient.updateIncome(id, income);
//        return incomeFeignClient.getAllIncomes(id).getBody();
    }

    @Override
    public boolean deleteIncomeById(int id) {

        try {
            incomeFeignClient.deleteIncomeById(id);
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }

}
