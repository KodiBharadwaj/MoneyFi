import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { AddBudgetDialogComponent } from '../add-budget-dialog/add-budget-dialog.component';
import { MatSelectModule } from '@angular/material/select';
import { ToastrService } from 'ngx-toastr';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { AddExpenseDialogComponent } from '../add-expense-dialog/add-expense-dialog.component';
import { NgChartsModule } from 'ng2-charts';
import { CountUpDirective } from '../shared/directives/count-up.directive';
import { UpdateBudgetDialogComponent } from '../update-budget-dialog/update-budget-dialog.component';


interface Budget {
  id: number;
  category: string;
  moneyLimit: number;
  currentSpending: number;
  remaining:number;
}

interface Expense {
  id: number;
  amount: number;
  date: string;
  category: string;
  description: string;  
  recurring: boolean;
}

@Component({
  selector: 'app-budgets',
  templateUrl: './budgets.component.html',
  styleUrls: ['./budgets.component.scss'],
  standalone: true,
  imports: [CommonModule,
    MatDialogModule,
    MatButtonModule,
    FormsModule,
    MatInputModule,
    AddExpenseDialogComponent,
    NgChartsModule,
    MatSelectModule,
    CountUpDirective]
})
export class BudgetsComponent {

  constructor(private httpClient:HttpClient, private router:Router, private dialog: MatDialog, private toastr:ToastrService){};
  baseUrl = "http://localhost:8765";

  totalBudget: number = 0;
  totalSpent: number = 0;
  budgets: Budget[] = [];


  expenses: Expense[] = [];
  filteredExpenses: Expense[] = [];
  loading: boolean = false;
  selectedYear: number = new Date().getFullYear();
  selectedMonth: number = 0; // 0 means all months
  selectedCategory: string = '';
  categories: string[] = [
    'Food', 'Travelling', 'Entertainment', 'Groceries', 'Shopping', 'Bills & utilities', 
    'House Rent', 'Emi and loans', 'Health & Medical', 'Miscellaneous'
  ];
  months: string[] = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ];
  
  availableYears: number[] = [];
  uniqueCategories: string[] = [];

  ngOnInit() {
    this.initializeFilters();
    
    // Set the default month to the current month (1-based index)
    this.selectedMonth = new Date().getMonth() + 1; // Current month in 1-based index
    this.selectedYear = new Date().getFullYear(); // Current year

    this.filterExpenses();
  }
  initializeFilters() {
    // Generate last 5 years
    const currentYear = new Date().getFullYear();
    this.availableYears = Array.from({length: 5}, (_, i) => currentYear - i);
  }

  loadExpensesData() {
    this.loading = true;

    let url: string;
    if (this.selectedMonth === 0) {
      url = `${this.baseUrl}/api/v1/expense/getExpenses/${this.selectedYear}/all/false`;
    } else {
      url = `${this.baseUrl}/api/v1/expense/getExpenses/${this.selectedMonth}/${this.selectedYear}/all/false`;
    }

    this.httpClient.get<Expense[]>(url).subscribe({
      next: (expenses) => {
        if (expenses && expenses.length > 0) {

          this.expenses = expenses;
          this.filteredExpenses = [...expenses]; // Initialize filteredExpenses with all expenses
          this.updateBudgetsWithExpenses();

        } else {
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
  }

  updateBudgetsWithExpenses() {
    const expenseMap = new Map<string, number>();

    this.filteredExpenses.forEach(expense => {
      expenseMap.set(
        expense.category,
        (expenseMap.get(expense.category) || 0) + expense.amount
      );
    });

    this.budgets.forEach(budget => {
      const spentInCategory = expenseMap.get(budget.category) || 0;
      budget.currentSpending = spentInCategory;
      budget.remaining = budget.moneyLimit - spentInCategory;
    });

    this.calculateTotals();
  }

  
  loadBudgetData() {
    this.loading = true;
    if(this.selectedCategory === '') this.selectedCategory = 'all';

    this.httpClient.get<Budget[]>(`${this.baseUrl}/api/v1/budget/getBudgetDetails/${this.selectedCategory}`).subscribe({
      next: (budgets) => {
        if(budgets === null){
          this.toastr.warning('You dont have budget', 'Please add Budget plan');
        }
        else {
          this.budgets = budgets;
          this.calculateTotals();
          // Load expenses after fetching budgets to update categories
          this.loadExpensesData();
        }
      },
      error: (error) => {
        console.error('Failed to load budget data:', error);
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

  calculateTotals() {
    this.totalBudget = this.budgets.reduce((sum, budget) => sum + budget.moneyLimit, 0);
    this.totalSpent = this.budgets.reduce((sum, budget) => sum + budget.currentSpending, 0);
  }

  
  calculateBudgetRemaining(data: Budget[]): void {
    data.forEach(budget => {
      budget.remaining = budget.moneyLimit - budget.currentSpending;
    });
  }
  

  getProgressPercentage(currentSpending: number, moneyLimit: number): number {
    return (currentSpending / moneyLimit) * 100;
  }

  getProgressColor(currentSpending: number, moneyLimit: number): string {
    const percentage = this.getProgressPercentage(currentSpending, moneyLimit);
    if (percentage >= 90) return '#f44336';  // Red
    if (percentage >= 75) return '#ff9800';  // Orange
    return '#4caf50';  // Green
  }

  addBudget() {
    const dialogRef = this.dialog.open(AddBudgetDialogComponent, {
      width: '500px',
      panelClass: 'income-dialog',
    });
  
    dialogRef.afterClosed().subscribe((result) => {
      if (result) {

        const categoryRequests: Promise<any>[] = result.categories.map((category: any) => {
          const categoryData = {
            ...category, // Includes fields like name, percentage, and amount
          };

          // Send individual POST request for each category
          return this.httpClient.post(`${this.baseUrl}/api/v1/budget/saveBudget`, categoryData).toPromise(); // Convert Observable to Promise
        });

        // Execute all POST requests
        Promise.all(categoryRequests)
          .then(() => {
            this.toastr.success('All categories added successfully');
            this.loadBudgetData(); // Refresh data if necessary
          })
          .catch((error) => {
            console.error('Failed to add one or more categories:', error);
            this.toastr.error('Some categories failed to add');
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
          });
      }
    });
  }
  
  
  
  updateBudget() {
    const dialogRef = this.dialog.open(UpdateBudgetDialogComponent, {
      width: '800px',
      data: { budgets: this.budgets }, // Pass all budgets to the dialog
    });
  
    dialogRef.afterClosed().subscribe((updatedBudgets) => {
      if (updatedBudgets) {
        // console.log(updatedBudgets);
        this.saveUpdatedBudgets(updatedBudgets);
      }
    });
  }
  
  // Save all updated budgets to the backend
  private saveUpdatedBudgets(updatedBudgets: any[]) {
    const token = sessionStorage.getItem('moneyfi.auth');
    let updateCount = 0;
  
    updatedBudgets.forEach((budget) => {
      // console.log(budget);
      this.httpClient.put(`${this.baseUrl}/api/v1/budget/${budget.id}`, budget).subscribe({
          next: () => {
            updateCount++;
  
            // Check if all budgets have been updated
            if (updateCount === updatedBudgets.length) {
              this.toastr.success('All budgets updated successfully');
              this.loadBudgetData(); // Refresh budgets after update
            }
          },
          error: (error) => {
            console.error('Failed to update budget:', error);
            this.toastr.error('Failed to update budget');
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
    });
  }
  
  

  filterExpenses() {
    this.loadBudgetData();
    // this.loadExpensesData();
  }

  resetFilters() {
    const today = new Date();
    this.selectedYear = today.getFullYear(); // Reset to the current year
    this.selectedMonth = today.getMonth() + 1; // Reset to the current month (1-based index)
    this.selectedCategory = ''; // Reset to all categories
    this.filterExpenses();
  }

}
