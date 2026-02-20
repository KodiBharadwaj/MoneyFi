import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { AddExpenseDialogComponent } from '../add-expense-dialog/add-expense-dialog.component';
import { ActivatedRoute, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { ChartConfiguration, ChartData } from 'chart.js';
import { NgChartsModule } from 'ng2-charts';
import { MatButtonModule } from '@angular/material/button';
import { FormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { CountUpDirective } from '../shared/directives/count-up.directive';
import { ConfirmDeleteDialogComponent } from '../confirm-delete-dialog/confirm-delete-dialog.component';
import { environment } from '../../environments/environment';
import { Category } from '../model/category-list';
import { CategoryService } from '../services/category.service';

interface Expense {
  id: number;
  amount: number;
  date: string;
  category: string;
  description: string;  
  recurring: boolean;
  totalAmount: number;
  totalCount: number;
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
  totalExpensesCount: number = 0;
  expenses: Expense[] = [];
  loading: boolean = false;
  recurringPercentage: number = 0;
  selectedYear: number = new Date().getFullYear();
  selectedMonth: number = 0;
  selectedCategory: string = '';
  categories: Category[] = [];
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
  isLoading = false;

  currentPage: number = 0;
  pageSize: number = 5;
  sortBy: string = '';
  sortOrder: 'asc' | 'desc' | '' = '';

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

  constructor(private httpClient: HttpClient, private dialog: MatDialog, private router:Router, 
    private toastr:ToastrService, private activateRoute: ActivatedRoute, private categoryService: CategoryService) {}

  baseUrl = environment.BASE_URL;


  ngOnInit() {
    this.categoryService.getExpenseCategories().subscribe(data => this.categories = data);
    this.activateRoute.queryParams.subscribe(params => {
      if (params['openDialog']) {
        this.addExpense();
      }
    });
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

    const payload = {
      category: this.selectedCategory === '' ? 'ALL' : this.selectedCategory,
      deleteStatus: false,
      date: this.getSelectedDate(),
      startIndex: this.currentPage * this.pageSize,
      threshold: this.pageSize,
      sortBy: this.sortBy,
      sortOrder: this.sortOrder,
      requestType: this.selectedMonth === 0 ? 'YEARLY' : 'MONTHLY'
    };

    this.httpClient.post<Expense[]>(`${this.baseUrl}/api/v1/transaction/expense/get-expenses`, payload).subscribe({
      next: (data) => {
        if (data && data.length > 0) {
          this.expenses = data;
          this.totalExpenses = data[0]?.totalAmount;
          this.totalExpensesCount = data[0]?.totalCount;
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
      },
      complete: () => {
        this.loading = false;
      }
    });

    this.httpClient.get<number>(`${this.baseUrl}/api/v1/transaction/income/totalIncome/${this.selectedMonth}/${this.selectedYear}`).subscribe({
      next: (totalIncome) => {
        this.totalIncome = totalIncome;
      },
      error: (error) => {
        console.error('Failed to load total income:', error);
      }
    });
  }
  
  onSort(column: string) {
    if (this.sortBy === column) {
      this.sortOrder = this.sortOrder === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = column;
      this.sortOrder = 'asc';
    }
    this.currentPage = 0;
    this.loadExpensesData();
  }

  getSortIcon(column: string): string {
    if (this.sortBy !== column) return 'fas fa-sort';
    return this.sortOrder === 'asc' ? 'fas fa-sort-up' : 'fas fa-sort-down';
  }

  nextPage() {
    this.currentPage++;
    this.loadExpensesData();
  }

  previousPage() {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadExpensesData();
    }
  }

  getSelectedDate(): string {
    if (this.selectedMonth === 0) {
      return `${this.selectedYear}-01-01`;
    }
    return `${this.selectedYear}-${this.selectedMonth.toString().padStart(2, '0')}-01`;
  }

  calculateTotalExpenses() {
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

        this.httpClient.post<Expense>(`${this.baseUrl}/api/v1/transaction/expense/saveExpense`, expenseData).subscribe({
          next: (newExpense) => {
            this.loadExpensesData()
            this.calculateTotalExpenses();
            this.updateChartData();
            this.toastr.success('Expense added successfully');
          },
          error: (error) => {
            console.error('Failed to load expense data:', error);
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

        this.httpClient.put<Expense>(`${this.baseUrl}/api/v1/transaction/expense/${expense.id}`,updatedExpenseData).subscribe({
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
          },
        });
      }
    });
  }
  
  formatDate(date: string | Date): string {
    const inputDate = new Date(date);
    const now = new Date(); // current time
    inputDate.setHours(now.getHours(), now.getMinutes(), now.getSeconds());

    const yyyy = inputDate.getFullYear();
    const mm = String(inputDate.getMonth() + 1).padStart(2, '0');
    const dd = String(inputDate.getDate()).padStart(2, '0');
    const hh = String(inputDate.getHours()).padStart(2, '0');
    const min = String(inputDate.getMinutes()).padStart(2, '0');
    const ss = String(inputDate.getSeconds()).padStart(2, '0');

    return `${yyyy}-${mm}-${dd}T${hh}:${min}:${ss}`;
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
        this.httpClient.delete<void>(`${this.baseUrl}/api/v1/transaction/expense`, { body : idsToDelete })
          .subscribe({
            next: () => {
              this.toastr.warning("Expense " + expenseDataFetch?.description + " has been deleted");
            },
            error: (error) => {
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

    this.httpClient.get<number>(`${this.baseUrl}/api/v1/transaction/income/availableBalance`).subscribe({
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
    this.isLoading = true;

    const payload = {
      category: this.selectedCategory === '' ? 'ALL' : this.selectedCategory,
      deleteStatus: false,
      date: this.getSelectedDate(),
      startIndex: 0,
      threshold: this.totalExpensesCount,
      sortBy: "",
      sortOrder: "",
      requestType: this.selectedMonth === 0 ? 'YEARLY' : 'MONTHLY'
    };

    this.httpClient.post(`${this.baseUrl}/api/v1/transaction/expense/get-expenses/excel-report`, payload, { responseType: 'blob' })
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
          this.isLoading = false;
        },
        error: (error) => {
          this.isLoading = false;
          console.error('Failed to generate report:', error);
          alert("Failed to generate the report. Please try again.");
        }
      });
  }

}
