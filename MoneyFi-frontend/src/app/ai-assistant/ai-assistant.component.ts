import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastrService } from 'ngx-toastr';
import { AiService } from '../services/ai.service';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';
import { FormatAdvicePipe } from '../shared/pipes/format-advice.pipe';

@Component({
  selector: 'app-ai-assistant',
  standalone: true,
  imports: [CommonModule, FormatAdvicePipe],
  templateUrl: './ai-assistant.component.html',
  styleUrl: './ai-assistant.component.scss'
})
export class AiAssistantComponent implements OnInit {
  advice: string = '';
  loading: boolean = false;
  error: string | null = null;
  baseUrl = "http://localhost:8765";
  currentYear = new Date().getFullYear();
  currentMonth = new Date().getMonth() + 1;

  constructor(
    private aiService: AiService,
    private httpClient: HttpClient,
    private router: Router,
    private toastr: ToastrService
  ) {}

  ngOnInit() {
    this.getFinancialAdvice();
  }

  getFinancialAdvice(): void {
    this.loading = true;
    this.error = null;
    const token = sessionStorage.getItem('finance.auth');

    this.httpClient.get<number>(`${this.baseUrl}/api/auth/token/${token}`).subscribe({
      next: (userId) => {
        // Create parallel requests for all financial data
        forkJoin({
          // Monthly data
          monthlyIncome: this.httpClient.get<number>(`${this.baseUrl}/api/income/${userId}/totalIncome/${this.currentMonth}/${this.currentYear}`),
          monthlyExpenses: this.httpClient.get<number>(`${this.baseUrl}/api/user/expenses/${userId}/totalExpenses/${this.currentMonth}/${this.currentYear}`),
          
          // Yearly data
          yearlyIncomes: this.httpClient.get<number[]>(`${this.baseUrl}/api/income/${userId}/monthlyTotalIncomesList/${this.currentYear}`),
          yearlyExpenses: this.httpClient.get<number[]>(`${this.baseUrl}/api/expense/${userId}/monthlyTotalExpensesList/${this.currentYear}`),
          yearlySavings: this.httpClient.get<number[]>(`${this.baseUrl}/api/user/${userId}/monthlySavingsInYear/${this.currentYear}`),
          cumulativeSavings: this.httpClient.get<number[]>(`${this.baseUrl}/api/user/${userId}/monthlyCumulativeSavingsInYear/${this.currentYear}`),
          
          // Budget data
          budgets: this.httpClient.get<any[]>(`${this.baseUrl}/api/user/${userId}/budgets`),
          budgetProgress: this.httpClient.get<number>(`${this.baseUrl}/api/user/${userId}/budgetProgress/${this.currentMonth}/${this.currentYear}`),
          
          // Goals and Net Worth
          totalCurrentGoalIncome: this.httpClient.get<number>(`${this.baseUrl}/api/goal/${userId}/totalCurrentGoalIncome`),
          totalTargetGoalIncome: this.httpClient.get<number>(`${this.baseUrl}/api/goal/${userId}/totalTargetGoalIncome`),
          remainingIncome: this.httpClient.get<number>(`${this.baseUrl}/api/income/${userId}/totalRemainingIncomeUpToPreviousMonth/${this.currentMonth}/${this.currentYear}`)
        }).subscribe({
          next: (data) => {
            // Calculate additional metrics
            const totalYearlyIncome = data.yearlyIncomes.reduce((a, b) => a + b, 0);
            const totalYearlyExpenses = data.yearlyExpenses.reduce((a, b) => a + b, 0);
            const averageMonthlySaving = data.yearlySavings.reduce((a, b) => a + b, 0) / data.yearlySavings.length;
            const latestCumulativeSaving = data.cumulativeSavings[data.cumulativeSavings.length - 1];
            const goalProgress = (data.totalCurrentGoalIncome / data.totalTargetGoalIncome) * 100;
            const netWorth = data.remainingIncome + (data.monthlyIncome - data.monthlyExpenses);

            const prompt = `
              As a financial advisor, analyze this user's financial data and provide comprehensive insights and advice:

              Current Month Overview:
              - Monthly Income: ₹${data.monthlyIncome}
              - Monthly Expenses: ₹${data.monthlyExpenses}
              - Current Month's Savings: ₹${data.monthlyIncome - data.monthlyExpenses}
              - Budget Adherence: ${data.budgetProgress}%

              Yearly Performance:
              - Total Income YTD: ₹${totalYearlyIncome}
              - Total Expenses YTD: ₹${totalYearlyExpenses}
              - Average Monthly Savings: ₹${averageMonthlySaving}
              - Cumulative Savings: ₹${latestCumulativeSaving}

              Financial Health Indicators:
              - Net Worth: ₹${netWorth}
              - Financial Goals Progress: ${goalProgress.toFixed(2)}%
              - Number of Budget Categories: ${data.budgets.length}

              Monthly Trends:
              - Income Trend: ${data.yearlyIncomes.join(', ')}
              - Expense Trend: ${data.yearlyExpenses.join(', ')}
              - Savings Trend: ${data.yearlySavings.join(', ')}

              Please provide:
              1. Overall financial health assessment
              2. Key strengths and areas of concern
              3. Specific recommendations for improvement
              4. Savings and investment suggestions
              5. Budget optimization advice
              6. Short-term and long-term financial planning tips

              Please format the response in clear sections with bullet points for easy reading.
              Use simple language and provide actionable insights.
            `;

            this.aiService.getAiResponse(prompt)
              .pipe(
                finalize(() => this.loading = false)
              )
              .subscribe({
                next: (response) => {
                  this.advice = response;
                  this.toastr.success('Financial analysis generated successfully');
                },
                error: (error) => {
                  this.error = 'Failed to generate financial analysis';
                  this.toastr.error(this.error, 'Error');
                  console.error('AI Service Error:', error);
                }
              });
          },
          error: (error) => {
            console.error('Failed to fetch financial data:', error);
            this.loading = false;
            this.error = 'Failed to fetch financial data';
            this.toastr.error(this.error, 'Error');
          }
        });
      },
      error: (error) => {
        console.error('Failed to fetch userId:', error);
        sessionStorage.removeItem('finance.auth');
        this.router.navigate(['login']);
      }
    });
  }
}