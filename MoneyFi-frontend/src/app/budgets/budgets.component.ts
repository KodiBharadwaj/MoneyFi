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
import { environment } from '../../environments/environment';


interface Budget {
  id: number;
  category: string;
  moneyLimit: number;
  currentSpending: number;
  progressPercentage:number;
  remaining:number;
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
  baseUrl = environment.BASE_URL;

  totalBudget: number = 0;
  totalSpent: number = 0;
  budgets: Budget[] = [];

  loading: boolean = false;
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
  
  loadBudgetData() {
    this.loading = true;
    if(this.selectedCategory === '') this.selectedCategory = 'all';

    this.httpClient.get<Budget[]>(`${this.baseUrl}/api/v1/budget/getBudgetDetails/${this.selectedCategory}/${this.selectedMonth}/${this.selectedYear}`).subscribe({
      next: (budgets) => {
        if(budgets === null){
          this.toastr.warning('You dont have budget', 'Please add Budget plan');
          this.loading = false;
        }
        else {
          this.budgets = budgets;
          this.calculateTotals();
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

  getProgressColor(percentage: number): string {
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
        console.log(result);

        this.httpClient.post(`${this.baseUrl}/api/v1/budget/saveBudget`, result).subscribe({
          next : () => {
            this.loadBudgetData();
            this.toastr.success('Budget added successfully')
          },
          error: (error) => {
            console.error('Failed to load total income:', error);
            this.toastr.error('Failed to add Budget! Please try later')
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
        })
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

        const modifiedBudgets = updatedBudgets.filter((updatedBudget: any) => {
          const originalBudget = this.budgets.find(b => b.id === updatedBudget.id);
          return originalBudget && originalBudget.moneyLimit !== updatedBudget.moneyLimit;
        });
  
        if (modifiedBudgets.length > 0) {
          console.log('Modified budgets:', modifiedBudgets);
          this.saveUpdatedBudgets(modifiedBudgets);
        } else {
          this.toastr.warning('No changes to update');
        }
      }
    });
  }
  
  private saveUpdatedBudgets(updatedBudgets: any[]) {

    this.httpClient.put(`${this.baseUrl}/api/v1/budget/updateBudget`, updatedBudgets).subscribe({
      next: () => {
        this.toastr.success('Budget updated successfully');
        this.loadBudgetData();
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
  }
  
  

  filterExpenses() {
    this.loadBudgetData();
  }

  resetFilters() {
    const today = new Date();
    this.selectedYear = today.getFullYear(); // Reset to the current year
    this.selectedMonth = today.getMonth() + 1; // Reset to the current month (1-based index)
    this.selectedCategory = ''; // Reset to all categories
    this.filterExpenses();
  }

}
