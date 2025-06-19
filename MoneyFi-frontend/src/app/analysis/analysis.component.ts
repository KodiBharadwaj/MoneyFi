import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChartConfiguration, ChartData } from 'chart.js';
import { NgChartsModule } from 'ng2-charts';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AiService } from '../services/ai.service';
import { ToastrService } from 'ngx-toastr';
import { finalize } from 'rxjs/operators';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-analysis',
  standalone: true,
  imports: [CommonModule, NgChartsModule],
  templateUrl: './analysis.component.html',
  styleUrl: './analysis.component.scss'
})
export class AnalysisComponent {
  baseUrl = environment.BASE_URL;

  // Add new properties
  isRadarFlipped = false;
  isMixedFlipped = false;
  isLineFlipped = false;

  radarAnalysis = '';
  mixedAnalysis = '';
  lineAnalysis = '';

  isRadarAnalysisLoading = false;
  isMixedAnalysisLoading = false;
  isLineAnalysisLoading = false;

  constructor(
    private httpClient: HttpClient,
    private router: Router,
    private aiService: AiService,
    private toastr: ToastrService
  ) {}

  ngOnInit() {
    this.loadChartData();
    this.loadMixedChartData();
    this.loadCumulativeData();
  }

  loadChartData() {
    
    // Get current month and year
    const currentDate = new Date();
    const currentMonth = currentDate.getMonth() + 1;
    const currentYear = currentDate.getFullYear();

    const expensesUrl = `${this.baseUrl}/api/v1/expense/getExpenses/${currentMonth}/${currentYear}/all/false`;
    const budgetsUrl = `${this.baseUrl}/api/v1/budget/getBudgetDetails/all/${currentMonth}/${currentYear}`;

        // Use forkJoin to make parallel requests
    forkJoin({
      expenses: this.httpClient.get<any[]>(expensesUrl),
      budgets: this.httpClient.get<any[]>(budgetsUrl)
    }).subscribe({
      next: ({ expenses, budgets }) => {
        // Process category-wise expenses
        const categoryExpenses = new Map<string, number>();
        expenses.forEach(expense => {
          const currentAmount = categoryExpenses.get(expense.category) || 0;
          categoryExpenses.set(expense.category, currentAmount + expense.amount);
        });

        // Process category-wise budgets
        const categoryBudgets = new Map<string, number>();
        budgets.forEach(budget => {
          categoryBudgets.set(budget.category, budget.moneyLimit);
        });

        // Get unique categories from both expenses and budgets
        const allCategories = Array.from(new Set([
          ...categoryExpenses.keys(),
          ...categoryBudgets.keys()
        ]));

        // Update radar chart data with both datasets
        this.radarChartData = {
          labels: allCategories,
          datasets: [
            {
              label: 'Monthly Budget',
              data: allCategories.map(category => categoryBudgets.get(category) || 0),
              fill: true,
              backgroundColor: 'rgba(255, 99, 132, 0.2)',
              borderColor: 'rgb(255, 99, 132)',
              pointBackgroundColor: 'rgb(255, 99, 132)',
              pointBorderColor: '#fff',
            },
            {
              label: 'Current Spending',
              data: allCategories.map(category => categoryExpenses.get(category) || 0),
              fill: true,
              backgroundColor: 'rgba(54, 162, 235, 0.2)',
              borderColor: 'rgb(54, 162, 235)',
              pointBackgroundColor: 'rgb(54, 162, 235)',
              pointBorderColor: '#fff',
            }
          ]
        };

        // Update chart options for better scale
        const maxValue = Math.max(
          ...Array.from(categoryBudgets.values()),
          ...Array.from(categoryExpenses.values())
        );

        this.radarChartOptions = {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              position: 'top',
              labels: {
                padding: 20,
                font: { size: 12 }
              }
            },
            title: {
              display: true,
              text: 'Monthly Budget vs. Actual Spending',
              padding: 20,
              font: {
                size: 16,
                weight: 'bold'
              }
            }
          },
          scales: {
            r: {
              min: 0,
              max: Math.ceil(maxValue / 1000) * 1000, // Round to nearest thousand
              ticks: {
                stepSize: Math.ceil(maxValue / 5000) * 1000, // Create about 5 steps
                display: false,
              }
            }
          }
        };
      },
      error: (error) => {
        console.error('Failed to load data:', error);
      }
    });
  }

  loadMixedChartData() {
    const currentYear = new Date().getFullYear();
    forkJoin({
      incomes: this.httpClient.get<number[]>(`${this.baseUrl}/api/v1/income/monthlyTotalIncomesList/${currentYear}`),
      expenses: this.httpClient.get<number[]>(`${this.baseUrl}/api/v1/expense/monthlyTotalExpensesList/${currentYear}`),
      savings: this.httpClient.get<number[]>(`${this.baseUrl}/api/v1/expense/monthlySavingsInYear/${currentYear}`)
    }).subscribe({
      next: ({ incomes, expenses, savings }) => {
        // Update mixed chart data with real values including savings
        this.mixedChartData = {
          labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
          datasets: [
            {
              type: 'bar',
              label: 'Expenses',
              data: expenses,
              borderColor: 'rgb(255, 99, 132)',
              backgroundColor: 'rgba(255, 99, 132, 0.2)',
              order: 2
            },
            {
              type: 'line',
              label: 'Income',
              data: incomes,
              fill: false,
              borderColor: 'rgb(54, 162, 235)',
              tension: 0.4,
              order: 0
            },
            {
              type: 'line',
              label: 'Savings',
              data: savings,
              fill: false,
              borderColor: 'rgb(75, 192, 192)',
              tension: 0.4,
              order: 1
            }
          ],
        };

        // Update chart options for better scale
        const maxValue = Math.max(...incomes, ...expenses, ...savings);
        this.mixedChartOptions = {
          ...this.mixedChartOptions,
          scales: {
            y: {
              beginAtZero: true,
              max: Math.ceil(maxValue / 10000) * 10000,
              ticks: {
                stepSize: Math.ceil(maxValue / 50000) * 10000
              }
            }
          },
          plugins: {
            title: {
              display: true,
              text: 'Monthly Income, Expenses & Savings Overview',
              padding: 20,
              font: {
                size: 16,
                weight: 'bold'
              }
            }
          }
        };
      },
      error: (error) => {
        console.error('Failed to load mixed chart data:', error);
      }
    });
  }

  // Mixed Chart Configuration
  public mixedChartData: ChartData<'bar' | 'line'> = {
    labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
    datasets: [
      {
        type: 'bar',
        label: 'Expenses',
        data: [50, 45, 60, 40, 35, 60, 50, 55, 70, 75, 80, 75],
        borderColor: 'rgb(255, 99, 132)',
        backgroundColor: 'rgba(255, 99, 132, 0.2)',
      },
      {
        type: 'line',
        label: 'Savings',
        data: [60, 55, 65, 50, 45, 70, 63, 67, 90, 95, 90, 85],
        fill: false,
        borderColor: 'rgb(54, 162, 235)',
      },
    ],
  };

  public mixedChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: true,
        position: 'top',
        labels: {
          padding: 20,
          font: {
            size: 12
          }
        }
      },
      title: {
        display: true,
        text: 'Monthly Finance Overview',
        padding: 20,
        font: {
          size: 16,
          weight: 'bold'
        }
      }
    },
    scales: {
      y: {
        beginAtZero: true
      }
    }
  };

  // Radar Chart Configuration
  public radarChartData: ChartData<'radar'> = {
    labels: ['Budgeting', 'Saving', 'Investing', 'Planning', 'Spending', 'Goals'],
    datasets: [
      {
        label: 'Target Status',
        data: [65, 59, 90, 81, 56, 55],
        fill: true,
        backgroundColor: 'rgba(255, 99, 132, 0.2)',
        borderColor: 'rgb(255, 99, 132)',
        pointBackgroundColor: 'rgb(255, 99, 132)',
        pointBorderColor: '#fff',
      },
      {
        label: 'Current Status',
        data: [28, 48, 40, 19, 96, 27],
        fill: true,
        backgroundColor: 'rgba(54, 162, 235, 0.2)',
        borderColor: 'rgb(54, 162, 235)',
        pointBackgroundColor: 'rgb(54, 162, 235)',
        pointBorderColor: '#fff',
      }
    ]
  };

  public radarChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'top',
        labels: {
          padding: 20,
          font: {
            size: 12
          }
        }
      },
      title: {
        display: true,
        text: 'Financial Health Analysis',
        padding: 20,
        font: {
          size: 16,
          weight: 'bold'
        }
      }
    },
    scales: {
      r: {
        min: 0,
        max: 100,
        ticks: {
          stepSize: 20
        }
      }
    }
  };

  // Add new chart configuration properties
  public lineChartData: ChartData<'line'> = {
    labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
    datasets: [
      {
        label: 'Cumulative Savings',
        data: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
        borderColor: 'rgb(75, 192, 192)',
        backgroundColor: 'rgba(75, 192, 192, 0.1)',
        fill: true,
        tension: 0.4,
        pointRadius: 4,
        pointHoverRadius: 6,
      }
    ]
  };

  public lineChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: true,
        position: 'top',
        labels: {
          padding: 20,
          font: { size: 12 }
        }
      },
      title: {
        display: true,
        text: 'Cumulative Savings Growth',
        padding: 20,
        font: {
          size: 16,
          weight: 'bold'
        }
      }
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: {
          callback: (value) => '₹' + value.toLocaleString()
        }
      }
    }
  };

  // Add new method to load cumulative data
  loadCumulativeData() {
    const currentYear = new Date().getFullYear();

    this.httpClient.get<number[]>(`${this.baseUrl}/api/v1/expense/monthlyCumulativeSavingsInYear/${currentYear}`).subscribe({
      next: (cumulativeData) => {
        this.lineChartData.datasets[0].data = cumulativeData;
        
        // Update chart options for better scale
        const maxValue = Math.max(...cumulativeData);
        this.lineChartOptions = {
          ...this.lineChartOptions,
          scales: {
            y: {
              beginAtZero: true,
              max: Math.ceil(maxValue / 10000) * 10000,
              ticks: {
                stepSize: Math.ceil(maxValue / 50000) * 10000,
                callback: (value) => '₹' + value.toLocaleString()
              }
            }
          }
        };
      },
      error: (error) => {
        console.error('Failed to load cumulative data:', error);
      }
    });
  }

  flipCard(chartType: 'radar' | 'mixed' | 'line'): void {
    switch (chartType) {
      case 'radar':
        if (!this.isRadarFlipped && !this.radarAnalysis) {
          this.generateAIAnalysis('radar');
        }
        this.isRadarFlipped = !this.isRadarFlipped;
        break;
      case 'mixed':
        if (!this.isMixedFlipped && !this.mixedAnalysis) {
          this.generateAIAnalysis('mixed');
        }
        this.isMixedFlipped = !this.isMixedFlipped;
        break;
      case 'line':
        if (!this.isLineFlipped && !this.lineAnalysis) {
          this.generateAIAnalysis('line');
        }
        this.isLineFlipped = !this.isLineFlipped;
        break;
    }
  }

  generateAIAnalysis(chartType: 'radar' | 'mixed' | 'line'): void {
    let analysis: string;
    let isLoading: boolean;
    let prompt: string;

    switch (chartType) {
      case 'radar':
        if (this.radarAnalysis) return;
        analysis = this.radarAnalysis;
        isLoading = this.isRadarAnalysisLoading;
        prompt = this.generateRadarPrompt();
        break;
      case 'mixed':
        if (this.mixedAnalysis) return;
        analysis = this.mixedAnalysis;
        isLoading = this.isMixedAnalysisLoading;
        prompt = this.generateMixedPrompt();
        break;
      case 'line':
        if (this.lineAnalysis) return;
        analysis = this.lineAnalysis;
        isLoading = this.isLineAnalysisLoading;
        prompt = this.generateLinePrompt();
        break;
    }

    this.setAnalysisLoading(chartType, true);

    this.aiService.getAiResponse(prompt)
      .pipe(
        finalize(() => this.setAnalysisLoading(chartType, false))
      )
      .subscribe({
        next: (response) => {
          this.setAnalysis(chartType, response);
          this.toastr.success('AI analysis generated successfully');
        },
        error: (error) => {
          console.error('AI Analysis Error:', error);
          this.toastr.error('Failed to generate AI analysis', 'Error');
          this.setAnalysis(chartType, 'Failed to generate analysis. Please try again.');
        }
      });
  }

  private setAnalysisLoading(chartType: 'radar' | 'mixed' | 'line', loading: boolean): void {
    switch (chartType) {
      case 'radar':
        this.isRadarAnalysisLoading = loading;
        break;
      case 'mixed':
        this.isMixedAnalysisLoading = loading;
        break;
      case 'line':
        this.isLineAnalysisLoading = loading;
        break;
    }
  }

  private setAnalysis(chartType: 'radar' | 'mixed' | 'line', analysis: string): void {
    switch (chartType) {
      case 'radar':
        this.radarAnalysis = analysis;
        break;
      case 'mixed':
        this.mixedAnalysis = analysis;
        break;
      case 'line':
        this.lineAnalysis = analysis;
        break;
    }
  }

  private generateRadarPrompt(): string {
    const budgetData = this.radarChartData.datasets[0].data;
    const spendingData = this.radarChartData.datasets[1].data;
    const categories = this.radarChartData.labels;

    return `
      Analyze this budget vs spending data:
      Categories: ${categories?.join(', ')}
      Budget Values: ${budgetData.join(', ')}
      Spending Values: ${spendingData.join(', ')}

      Please provide insights about:
      1. Budget utilization across categories
      2. Areas of overspending or underspending
      3. Recommendations for budget adjustments
      4. Overall budget management effectiveness

      Give response in bullet points and without bold text just give in normal plaintext format.
      Please keep the analysis concise, use simple language, and focus on actionable insights.
    `;
  }

  private generateMixedPrompt(): string {
    const incomeData = this.mixedChartData.datasets[1].data;
    const expenseData = this.mixedChartData.datasets[0].data;
    const savingsData = this.mixedChartData.datasets[2].data;

    return `
      Analyze this monthly financial flow data:
      Income Values: ${incomeData.join(', ')}
      Expense Values: ${expenseData.join(', ')}
      Savings Values: ${savingsData.join(', ')}

      Please provide insights about:
      1. Income vs expense patterns
      2. Saving rate and consistency
      3. Monthly financial management
      4. Areas for improvement

      Please provide the analysis in plain text format, without any bold text, bullet points, or special formatting.
      Please keep the analysis concise, use simple language, and focus on actionable insights.
    `;
  }

  private generateLinePrompt(): string {
    const cumulativeData = this.lineChartData.datasets[0].data;
    const totalSavings = cumulativeData[cumulativeData.length - 1];

    return `
      I have a user's monthly cumulative savings data for the year ${new Date().getFullYear()}:
      Monthly Cumulative Values (in ₹): ${cumulativeData.join(', ')}
      Total Savings: ₹${totalSavings}

      Please analyze this financial data and provide insights about:
      1. Saving patterns and trends
      2. Monthly saving consistency
      3. Areas of improvement if any
      4. General financial health based on the saving pattern

      Give response in bullet points and without bold text just give in normal plaintext format.
      Please keep the analysis concise, use simple language, and focus on actionable insights.
    `;
  }
}
