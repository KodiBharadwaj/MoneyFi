import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CountUpDirective } from '../shared/directives/count-up.directive';
import { ToastrService } from 'ngx-toastr';

interface FinancialSummary {
  income: number;
  expenses: number;
  budget: number;
  netWorth: number;
  budgetProgress: number;
  goalsProgress: number;
  username: string;
}

interface Budget {
  id: number;
  category: string;
  moneyLimit: number;
  currentSpending: number;
  remaining:number;
}

@Component({
  selector: 'app-overview',
  standalone: true,
  imports: [
    CommonModule, 
    MatCardModule, 
    MatProgressBarModule, 
    MatIconModule,
    MatButtonModule,
    CountUpDirective
  ],
  templateUrl: './overview.component.html',
  styleUrls: ['./overview.component.css']
})
export class OverviewComponent implements OnInit {
  // financialQuote = "Wealth is not about having a lot of money, it's about having a lot of options. – Chris Rock";
  
  summary: FinancialSummary = {
    income: 0,
    expenses: 0,
    budget: 0,
    netWorth: 0,
    budgetProgress: 0,
    goalsProgress: 0,
    username: ''
  };
  // Set the default month to the current month (1-based index)
  thisMonth = new Date().getMonth() + 1; // Current month in 1-based index
  thisYear = new Date().getFullYear(); // Current year

  constructor(private router: Router, private httpClient:HttpClient, private toastr:ToastrService) {}
  baseUrl = "http://localhost:8765";

  ngOnInit() {
    this.loadFinancialData();
  }

  private loadFinancialData() {
    const token = sessionStorage.getItem('finance.auth');
    console.log(token)

    this.httpClient.get<number>(`${this.baseUrl}/auth/token/${token}`).subscribe({
      next : (userId) => {
        console.log(userId);

      this.httpClient.get(`${this.baseUrl}/api/profile/getName/${userId}`, {responseType : 'text'}).subscribe({
        next : (userName) => {
          this.summary.username = userName;
        },
        error : (error) => {
          console.log('Failed to get the user name', error);
        }
      })

      this.httpClient.get<number>(`${this.baseUrl}/api/income/${userId}/totalIncome/${this.thisMonth}/${this.thisYear}`).subscribe({
        next : (totalIncome) => {
          this.summary.income = totalIncome;

          this.httpClient.get<number>(`${this.baseUrl}/api/expense/${userId}/totalExpense/${this.thisMonth}/${this.thisYear}`).subscribe({
            next : (totalExpense) => {
              this.summary.expenses = totalExpense;
            },
            error : (error) => {
              console.log('Failed to get the expense details', error);
            }
          })
        },
        error : (error) => {
          console.log('Failed to get the income details', error);
        }
      })


      this.httpClient.get<Budget[]>(`${this.baseUrl}/api/budget/${userId}`).subscribe({
        next : (budgetList) => {
          const totalBudget = budgetList.reduce((acc, budget) => acc + budget.moneyLimit, 0);
          this.summary.budget = totalBudget;
        },
        error : (error) => {
          console.log('Failed to get the total goal income details', error);
        }
      })


      this.httpClient.get<number>(`${this.baseUrl}/api/income/${userId}/totalRemainingIncomeUpToPreviousMonth/${this.thisMonth}/${this.thisYear}`).subscribe({
        next : (totalRemainingIncome) => {
          // console.log(totalRemainingIncome);
          this.httpClient.get<number>(`${this.baseUrl}/api/goal/${userId}/totalCurrentGoalIncome`).subscribe({
            next : (totalGoalIncome) => {
              // console.log(totalGoalIncome);
              this.summary.netWorth = totalRemainingIncome + (this.summary.income - this.summary.expenses);
            }
          })
        },
        error : (error) => {
          console.log('Failed to get the total goal income details', error);
        }
      })

      this.httpClient.get<number>(`${this.baseUrl}/api/budget/${userId}/budgetProgress/${this.thisMonth}/${this.thisYear}`).subscribe({
        next : (totalBudgetIncome) => {
          // console.log(totalBudgetIncome);
          this.summary.budgetProgress = parseFloat((totalBudgetIncome * 100).toFixed(2));;
        },
        error : (error) => {
          console.log('Failed to get the budget progress details', error);
        }
      })


      this.httpClient.get<number>(`${this.baseUrl}/api/goal/${userId}/totalCurrentGoalIncome`).subscribe({
        next : (totalCurrentGoalIncome) => {
          this.httpClient.get<number>(`${this.baseUrl}/api/goal/${userId}/totalTargetGoalIncome`).subscribe({
            next: (totalTargetGoalIncome) => {
              this.summary.goalsProgress = parseFloat(((totalCurrentGoalIncome/totalTargetGoalIncome)*100).toFixed(2))
            }
          })
        },
        error : (error) => {
          console.log('Failed to get the total goal income details', error);
        }
      })
        
      },
      error : (error) => {
        console.log('Failed to get the user Id', error);
        alert("Session timed out! Please login again");
        sessionStorage.removeItem('finance.auth');
        this.router.navigate(['login']);
      }
    })

  }


  addExpenses() {
    this.router.navigate(['dashboard/expenses']);
  }

  createBudget() {
    this.router.navigate(['dashboard/budgets']);
  }

  viewReports() {
    this.router.navigate(['dashboard/ai-assistant']);
  }

}