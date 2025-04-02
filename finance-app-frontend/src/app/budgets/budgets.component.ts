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
  
    // this.loadBudgetData();
    this.filterExpenses();
  }
  initializeFilters() {
    // Generate last 5 years
    const currentYear = new Date().getFullYear();
    this.availableYears = Array.from({length: 5}, (_, i) => currentYear - i);
  }

  loadExpensesData() {
    this.loading = true;
    const token = sessionStorage.getItem('finance.auth');
  
    this.httpClient.get<number>(`${this.baseUrl}/auth/token/${token}`).subscribe({
      next: (userId) => {
        let url: string;
        if (this.selectedMonth === 0) {
          url = `${this.baseUrl}/api/expense/${userId}/${this.selectedYear}/false`;
        } else {
          url = `${this.baseUrl}/api/expense/${userId}/${this.selectedMonth}/${this.selectedYear}/false`;
        }
  
        this.httpClient.get<Expense[]>(url).subscribe({
          next: (expenses) => {
            if (expenses && expenses.length > 0) {
              // console.log(expenses);

              this.expenses = expenses;
              this.filteredExpenses = [...expenses]; // Initialize filteredExpenses with all expenses
              this.updateBudgetsWithExpenses();

            } else {
              this.toastr.warning('No expenses found for the selected filters.', 'No Data');
            }
          },
          error: (error) => {
            console.error('Failed to load Expense data:', error);
            this.toastr.error('Failed to load expenses', 'Error');
          },
          complete: () => {
            this.loading = false;
          }
        });
      },
      error: (error) => {
        console.error('Failed to fetch userId:', error);
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

  applyFilters() {
    // Filter by category if a category is selected
    if (this.selectedCategory) {
      this.expenses = this.expenses.filter(expense => expense.category === this.selectedCategory);
    }
  }
  
  loadBudgetData() {
    this.loading = true;
    const token = sessionStorage.getItem('finance.auth');
  
    this.httpClient.get<number>(`${this.baseUrl}/auth/token/${token}`).subscribe({
      next: (userId) => {
        this.httpClient.get<Budget[]>(`${this.baseUrl}/api/budget/${userId}`).subscribe({
          next: (budgets) => {
            if(budgets === null){
              this.toastr.warning('You dont have budget', 'Please add Budget plan');
            }
            else {
              this.budgets = budgets;
              // console.log(this.budgets);
              this.calculateTotals();
              // Load expenses after fetching budgets to update categories
              this.loadExpensesData();
            }
          },
          error: (error) => {
            // console.error('Failed to load Budget data:', error);
            // if(error.status === 204){
            //   this.toastr.warning('You dont have budget', 'Please add Budget plan')
            // }
            this.toastr.error('Failed to load budgets', 'Error');
          },
          complete: () => {
            this.loading = false;
          }
        });
      },
      error: (error) => {
        console.error('Failed to fetch userId:', error);
        alert("Session timed out! Please login again");
        sessionStorage.removeItem('finance.auth');
        this.router.navigate(['login']);
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

  // addBudget() {
  //   // Implement add budget logic
  //   console.log('Add budget clicked');
  // }
  addBudget() {
    const dialogRef = this.dialog.open(AddBudgetDialogComponent, {
      width: '500px',
      panelClass: 'income-dialog',
    });
  
    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        const token = sessionStorage.getItem('finance.auth');
        if (token) {
          this.httpClient.get<number>(`${this.baseUrl}/auth/token/${token}`).subscribe({
            next: (userId) => {
              console.log(userId);
  
              // Explicitly type the category requests
              const categoryRequests: Promise<any>[] = result.categories.map((category: any) => {
                const categoryData = {
                  userId: userId,
                  ...category, // Includes fields like name, percentage, and amount
                };
  
                // Send individual POST request for each category
                return this.httpClient.post(`${this.baseUrl}/api/budget/${userId}`, categoryData, {
                  headers: {
                    Authorization: `Bearer ${token}`,
                  },
                }).toPromise(); // Convert Observable to Promise
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
                });
            },
            error: (error) => {
              console.error('Failed to fetch userId:', error);
              this.toastr.error('Failed to retrieve user ID');
            },
          });
        }
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
    const token = sessionStorage.getItem('finance.auth');
    let updateCount = 0;
  
    updatedBudgets.forEach((budget) => {
      // console.log(budget);
      this.httpClient
        .put(`${this.baseUrl}/api/budget/${budget.id}`, budget, {
          headers: { Authorization: `Bearer ${token}` },
        })
        .subscribe({
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
          },
        });
    });
  }
  
  
  updateUniqueCategories() {
    // Ensure "All Categories" is an option
    this.uniqueCategories = ['', ...new Set(this.expenses.map(expense => expense.category))];
  }

  filterExpenses() {
    this.loadBudgetData();
    this.loadExpensesData();
  }

  resetFilters() {
    const today = new Date();
    this.selectedYear = today.getFullYear(); // Reset to the current year
    this.selectedMonth = today.getMonth() + 1; // Reset to the current month (1-based index)
    this.selectedCategory = ''; // Reset to all categories
    this.filterExpenses();
  }
 
  

}
