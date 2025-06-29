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
import { environment } from '../../environments/environment';

interface FinancialSummary {
  income: number;
  expenses: number;
  availableBalance : number;
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
  
  summary: FinancialSummary = {
    income: 0,
    expenses: 0,
    availableBalance : 0,
    budget: 0,
    netWorth: 0,
    budgetProgress: 0,
    goalsProgress: 0,
    username: ''
  };

  thisMonth = new Date().getMonth() + 1; // Current month in 1-based index
  thisYear = new Date().getFullYear(); // Current year

  loading: boolean = false;

  constructor(private router: Router, private httpClient:HttpClient, private toastr:ToastrService) {}
  baseUrl = environment.BASE_URL;

  ngOnInit() {
    this.loadFinancialData();
  }

  private loadFinancialData() {
    this.loading = true;

    const storedName = sessionStorage.getItem('Name');
    if (storedName !== null) {
      this.summary.username = storedName;
    }

    else {
      this.httpClient.get(`${this.baseUrl}/api/v1/userProfile/getName`, {responseType : 'text'}).subscribe({
        next : (userName : string) => {
          sessionStorage.setItem('Name', userName);
          this.summary.username = userName;
        },
        error : (error) => {
          console.log('Failed to get the user name', error);
        }
      })
    }
    
    this.httpClient.get<any>(`${this.baseUrl}/api/v1/income/overview-details/${this.thisMonth}/${this.thisYear}`).subscribe({
      next : (response) => {
        this.loading = false;
        this.summary.availableBalance = response.availableBalance;
        this.summary.expenses = response.totalExpense;
        this.summary.budget = response.totalBudget;
        this.summary.budgetProgress = parseFloat((response.budgetProgress).toFixed(2));
        this.summary.netWorth = response.totalGoalIncome;
        this.summary.goalsProgress = parseFloat((response.goalProgress).toFixed(2))
        
        if(this.summary.availableBalance === 0 && this.summary.expenses === 0 && this.summary.budget === 0 
            && this.summary.budgetProgress === 0 && this.summary.netWorth === 0 && this.summary.goalsProgress === 0){
              this.toastr.warning("No data found!");
            }
      },
      error : (error) => {
        this.loading = false;
        console.log('Failed to get the income details', error);
      }
    })
  }


  addExpenses() {
    this.router.navigate(['dashboard/expenses']);
  }

  createBudget() {
    this.router.navigate(['dashboard/budgets']);
  }

  viewAnalysis() {
    this.router.navigate(['dashboard/analysis']);
  }

}