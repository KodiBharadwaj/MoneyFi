import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { AddExpenseDialogComponent } from '../add-expense-dialog/add-expense-dialog.component';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { ChartConfiguration, ChartData } from 'chart.js';
import { NgChartsModule } from 'ng2-charts';
import { MatButtonModule } from '@angular/material/button';
import { FormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { CountUpDirective } from '../shared/directives/count-up.directive';
import { ConfirmDeleteDialogComponent } from '../confirm-delete-dialog/confirm-delete-dialog.component';

interface Expense {
  id: number;
  amount: number;
  date: string;
  category: string;
  description: string;  
  recurring: boolean;
}

interface FinancialSummary {
  income: number;
  expenses: number;
}

@Component({
  selector: 'app-expenses',
  templateUrl: './expenses.component.html',
  styleUrls: ['./expenses.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    FormsModule,
    MatInputModule,
    AddExpenseDialogComponent,
    NgChartsModule,
    MatSelectModule,
    CountUpDirective
  ]
})
export class ExpensesComponent {
  totalExpenses: number = 0;
  expenses: Expense[] = [];
  loading: boolean = false;
  recurringPercentage: number = 0;
  selectedYear: number = new Date().getFullYear();
  selectedMonth: number = 0; // 0 means all months
  selectedCategory: string = '';
  categories: string[] = [
    'Food', 'Travelling', 'Entertainment', 'Groceries', 'Shopping', 'Bills & utilities', 
    'House Rent', 'Emi and loans', 'Health & Medical', 'Goal', 'Miscellaneous'
  ];
  months: string[] = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ];
  
  availableYears: number[] = [];
  uniqueCategories: string[] = [];
  totalIncome: number = 0;
  spentPercentage: number = 0;
  thisMonthincomeLeft: number = 0;
  overallincomeLeft: number = 0;

  public pieChartData: ChartData<'pie' | 'doughnut', number[], string> = {
    labels: [],
    datasets: [{
      data: [],
      backgroundColor: [
        '#FF6384',
        '#36A2EB',
        '#FFCE56',
        '#4BC0C0',
        '#9966FF',
        '#FF9F40'
      ]
    }]
  };

  public pieChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: true,
        position: 'right',
      }
    },
  };

  constructor(private httpClient: HttpClient, private dialog: MatDialog, private router:Router, private toastr:ToastrService) {}

  baseUrl = "http://localhost:8765";


  ngOnInit() {
    this.initializeFilters();
    
    // Set the default month to the current month (1-based index)
    this.selectedMonth = new Date().getMonth() + 1; // Current month in 1-based index
    this.selectedYear = new Date().getFullYear(); // Current year
  
    this.loadExpensesData();
  }
  

  initializeFilters() {
    // Generate last 5 years
    const currentYear = new Date().getFullYear();
    this.availableYears = Array.from({length: 5}, (_, i) => currentYear - i);
  }

  loadExpensesData() {
    this.loading = true;

    let url: string;
    if(this.selectedCategory === '') this.selectedCategory = 'all';
    if (this.selectedMonth === 0) {
      // Fetch all expenses for the selected year
      url = `${this.baseUrl}/api/v1/expense/getExpenses/${this.selectedYear}/${this.selectedCategory}/false`;
    } else {
      // Fetch expenses for the specific month and year
      url = `${this.baseUrl}/api/v1/expense/getExpenses/${this.selectedMonth}/${this.selectedYear}/${this.selectedCategory}/false`;
    }

    this.httpClient.get<Expense[]>(url).subscribe({
      next: (data) => {
        if (data && data.length > 0) {
          this.expenses = data;
          this.calculateTotalExpenses();
          this.updateChartData();
        } else {
          this.expenses = [];
          this.calculateTotalExpenses();
          this.toastr.warning('No expenses found for the selected filters.', 'No Data');
        }
      },
      error: (error) => {
        console.error('Failed to load expense data:', error);
        if(error.status === 401){
            if (error.error === 'TokenExpired') {
              alert('Your session has expired. Please login again.');
              sessionStorage.removeItem('moneyfi.auth');
              this.router.navigate(['/']);
            } else if(error.error === 'Token is blacklisted'){
              alert('Your session has expired. Please login again.');
              sessionStorage.removeItem('moneyfi.auth');
              this.router.navigate(['/']);
            }
            else if(error.error === 'AuthorizationFailed'){
              alert('Service Unavailable!! Please try later');
            }
          } else if (error.status === 503){
            alert('Service Unavailable!! Please try later');
          }
      },
      complete: () => {
        this.loading = false;
      }
    });

    this.httpClient.get<number>(`${this.baseUrl}/api/v1/income/totalIncome/${this.selectedMonth}/${this.selectedYear}`).subscribe({
      next: (totalIncome) => {
        this.totalIncome = totalIncome;
        this.calculateSpentPercentage();
      },
      error: (error) => {
        console.error('Failed to load total income:', error);
        if(error.status === 401){
            if (error.error === 'TokenExpired') {
              alert('Your session has expired. Please login again.');
              sessionStorage.removeItem('moneyfi.auth');
              this.router.navigate(['/']);
            } else if(error.error === 'Token is blacklisted'){
              alert('Your session has expired. Please login again.');
              sessionStorage.removeItem('moneyfi.auth');
              this.router.navigate(['/']);
            }
            else if(error.error === 'AuthorizationFailed'){
              alert('Service Unavailable!! Please try later');
            }
          } else if (error.status === 503){
            alert('Service Unavailable!! Please try later');
          }
      }
    });
  }
  

  calculateTotalExpenses() {
    this.totalExpenses = this.expenses.reduce((sum, expense) => sum + expense.amount, 0);
    this.calculateSpentPercentage();
  }

  addExpense() {
    const dialogRef = this.dialog.open(AddExpenseDialogComponent, {
      width: '500px',
      panelClass: 'income-dialog',
    });
  
    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        // Check if adding this expense would exceed income
        if (result.amount > this.overallincomeLeft) {
          this.toastr.error('Cannot add expense. Amount exceeds Available Income.', 'Insufficient Income');
          return;
        }

        const formattedDate = this.formatDate(result.date);
        const expenseData = {
          ...result,
          date: formattedDate,
        };

        this.httpClient.post<Expense>(`${this.baseUrl}/api/v1/expense/saveExpense`, expenseData).subscribe({
          next: (newExpense) => {
            this.expenses.push(newExpense);
            this.calculateTotalExpenses();
            this.updateChartData();
            this.toastr.success('Expense added successfully');
          },
          error: (error) => {
            console.error('Failed to load expense data:', error);
            if(error.status === 401){
            if (error.error === 'TokenExpired') {
              alert('Your session has expired. Please login again.');
              sessionStorage.removeItem('moneyfi.auth');
              this.router.navigate(['/']);
            } else if(error.error === 'Token is blacklisted'){
              alert('Your session has expired. Please login again.');
              sessionStorage.removeItem('moneyfi.auth');
              this.router.navigate(['/']);
            }
            else if(error.error === 'AuthorizationFailed'){
              alert('Service Unavailable!! Please try later');
            }
          } else if (error.status === 503){
            alert('Service Unavailable!! Please try later');
          }
          },
          complete: () => {
            this.loading = false;
          }
        });

      }
    });
  }

  updateExpense(expense: Expense) {
    const dialogRef = this.dialog.open(AddExpenseDialogComponent, {
      width: '500px',
      panelClass: 'income-dialog',
      data: { ...expense, isUpdate: true },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        // Calculate what the total expenses would be after this update
        const updatedTotalExpenses = this.totalExpenses - expense.amount + result.amount;
        
        // Check if the update would exceed income
        // if (updatedTotalExpenses > this.totalIncome) {
        //   this.toastr.error('Cannot update expense. Amount exceeds available income.', 'Insufficient Income');
        //   return;
        // }

        const formattedDate = this.formatDate(result.date);
        const updatedExpenseData = {
          ...result,
          date: formattedDate,
        };

        this.httpClient.put<Expense>(`${this.baseUrl}/api/v1/expense/${expense.id}`,updatedExpenseData).subscribe({
          next: (updatedExpense) => {
            // console.log('Expense updated successfully:', updatedExpense);
            if(updatedExpense){
              this.loadExpensesData();
              this.toastr.success('Expense updated successfully');
            } else {
              this.toastr.warning('No changes to update');
            }
          },
          error: (error) => {
            console.error('Failed to update Expense:', error);
            this.toastr.error('Failed to update expense', 'Error');
            if(error.status === 401){
            if (error.error === 'TokenExpired') {
              alert('Your session has expired. Please login again.');
              sessionStorage.removeItem('moneyfi.auth');
              this.router.navigate(['/']);
            } else if(error.error === 'Token is blacklisted'){
              alert('Your session has expired. Please login again.');
              sessionStorage.removeItem('moneyfi.auth');
              this.router.navigate(['/']);
            }
            else if(error.error === 'AuthorizationFailed'){
              alert('Service Unavailable!! Please try later');
            }
          } else if (error.status === 503){
            alert('Service Unavailable!! Please try later');
          }
          },
        });
      }
    });
  }
  
  formatDate(date: string): string {
    const d = new Date(date);
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, '0'); // Months are zero-based
    const dd = String(d.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
  }

  deleteExpense(expenseId: number): void {
    const expenseDataFetch = this.expenses.find(i=>i.id === expenseId);
      const dialogRef = this.dialog.open(ConfirmDeleteDialogComponent, {
        width: '400px',
        panelClass: 'custom-dialog-container',
        data:{...expenseDataFetch, isExpense:true}
      });
  
      dialogRef.afterClosed().subscribe((result) => {
        if (result) {
          const index = this.expenses.findIndex(i=>i.id === expenseId);
        if (index !== -1) {
          this.expenses.splice(index, 1); // Remove the item at the found index
        }
        this.calculateTotalExpenses();
        this.updateChartData();
        const idsToDelete = [expenseId]; 
        this.httpClient.delete<void>(`${this.baseUrl}/api/v1/expense`, { body : idsToDelete })
          .subscribe({
            next: () => {
              this.toastr.warning("Expense " + expenseDataFetch?.description + " has been deleted");
            },
            error: (err) => {
              console.error('Error deleting expense:', err);
            }
          });
        }
      });
  }

  private updateChartData() {
    const categoryMap = new Map<string, number>();
    
    this.expenses.forEach(expense => {
      const currentAmount = categoryMap.get(expense.category) || 0;
      categoryMap.set(expense.category, currentAmount + expense.amount);
    });

    // Update chart data
    this.pieChartData = {
      labels: Array.from(categoryMap.keys()),
      datasets: [{
        data: Array.from(categoryMap.values()),
        backgroundColor: [
          '#FF6384',
          '#36A2EB',
          '#FFCE56',
          '#4BC0C0',
          '#9966FF',
          '#FF9F40'
        ]
      }]
    };

    // Calculate recurring vs one-time ratio
    const recurringTotal = this.expenses
      .filter(expense => expense.recurring)
      .reduce((sum, expense) => sum + expense.amount, 0);
    
    this.recurringPercentage = this.totalExpenses > 0 
      ? Math.round((recurringTotal / this.totalExpenses) * 100)
      : 0;
  }

  filterExpenses() {
    this.loadExpensesData();
  }

  resetFilters() {
    const today = new Date();
    this.selectedYear = today.getFullYear(); // Reset to the current year
    this.selectedMonth = today.getMonth() + 1; // Reset to the current month (1-based index)
    this.selectedCategory = ''; // Reset to all categories
    this.filterExpenses();
  }

  getProgressColor(spent: number, total: number): string {
    const percentage = (spent / total) * 100;
    if (percentage >= 90) return '#E54A00';  //  Red E53935
    if (percentage >= 50) return '#FB8C00';  //  Orange 
    return '#FFB300';  // Yellow FFB300
  }

  private calculateSpentPercentage() {
    this.spentPercentage = this.totalIncome > 0 
      ? parseFloat(((this.totalExpenses / this.totalIncome) * 100).toFixed(2))
      : 0;
    
    if(this.totalIncome - this.totalExpenses >= 0){
      this.thisMonthincomeLeft = this.totalIncome - this.totalExpenses;
    }
    else {
      this.thisMonthincomeLeft = 0;
    }

    this.httpClient.get<number>(`${this.baseUrl}/api/v1/income/availableBalance`).subscribe({
      next : (availableBalance) => {
        this.overallincomeLeft = availableBalance;
      },
      error : (error) => {
        console.log('Failed to get the total available income details', error);
      }
    })
  }
  
  getSpendingStatusMessage(percentage: number): string {
    if (percentage >= 90) {
      return 'Warning: Spending exceeds 90% of income. Consider reducing expenses.';
    } else if (percentage >= 75) {
      return 'Caution: Approaching income limit. Review your spending.';
    } else if (percentage >= 50) {
      return 'Moderate spending. You\'re maintaining good balance.';
    } else {
      return 'Great job! Your spending is well under control.';
    }
  }


  generateReport() {

    let url: string;
    if (this.selectedMonth === 0) {
      url = `${this.baseUrl}/api/v1/expense/${this.selectedYear}/${this.selectedCategory}/generateYearlyReport`;
    } else {
      url = `${this.baseUrl}/api/v1/expense/${this.selectedMonth}/${this.selectedYear}/${this.selectedCategory}/generateMonthlyReport`;
    }

    this.httpClient.get(url, { responseType: 'blob' })
      .subscribe({
        next: (response) => {
          // Trigger File Download
          const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = `Monthly_Report_04_2025.xlsx`;
          document.body.appendChild(a);
          a.click();
          window.URL.revokeObjectURL(url);
          document.body.removeChild(a);
        },
        error: (error) => {
          console.error('Failed to generate report:', error);
          alert("Failed to generate the report. Please try again.");
          if(error.status === 401){
            if (error.error === 'TokenExpired') {
              alert('Your session has expired. Please login again.');
              sessionStorage.removeItem('moneyfi.auth');
              this.router.navigate(['/']);
            } else if(error.error === 'Token is blacklisted'){
              alert('Your session has expired. Please login again.');
              sessionStorage.removeItem('moneyfi.auth');
              this.router.navigate(['/']);
            }
            else if(error.error === 'AuthorizationFailed'){
              alert('Service Unavailable!! Please try later');
            }
          } else if (error.status === 503){
            alert('Service Unavailable!! Please try later');
          }
        }
      });
  }

}
