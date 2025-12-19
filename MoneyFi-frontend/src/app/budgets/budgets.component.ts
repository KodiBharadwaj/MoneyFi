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
import { ConfirmDeleteDialogComponent } from '../confirm-delete-dialog/confirm-delete-dialog.component';
import { MatIconModule } from '@angular/material/icon';


interface Budget {
  id: number;
  category: string;
  moneyLimit: number;
  currentSpending: number;
  progressPercentage:number;
  remaining:number;
  createdAt: string;
  updatedAt: string;
}

@Component({
  selector: 'app-budgets',
  templateUrl: './budgets.component.html',
  styleUrls: ['./budgets.component.scss'],
  standalone: true,
  imports: [CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
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

    this.httpClient.get<Budget[]>(`${this.baseUrl}/api/v1/wealth-core/budget/${this.selectedCategory}/${this.selectedMonth}/${this.selectedYear}/get`).subscribe({
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
      error: (err) => {
        console.error('Failed to load budget data:', err);
        this.loading = false;
        try {
          const errorObj = typeof err.error === 'string' ? JSON.parse(err.error) : err.error;
          this.toastr.error(errorObj.message);
        } catch (e) {
          console.error('Failed to parse error:', err.error);
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

        this.httpClient.post(`${this.baseUrl}/api/v1/wealth-core/budget/save`, result).subscribe({
          next : () => {
            this.loadBudgetData();
            this.toastr.success('Budget added successfully')
          },
          error: (err) => {
            console.error('Failed to load total income:', err);
            try {
              const errorObj = typeof err.error === 'string' ? JSON.parse(err.error) : err.error;
              this.toastr.error(errorObj.message);
            } catch (e) {
              console.error('Failed to parse error:', err.error);
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

    this.httpClient.put(`${this.baseUrl}/api/v1/wealth-core/budget/update`, updatedBudgets).subscribe({
      next: () => {
        this.toastr.success('Budget updated successfully');
        this.loadBudgetData();
      },
      error: (err) => {
        console.error('Failed to update budget:', err);
        try {
          const errorObj = typeof err.error === 'string' ? JSON.parse(err.error) : err.error;
          this.toastr.error(errorObj.message);
        } catch (e) {
          console.error('Failed to parse error:', err.error);
        }
      },
    });
  }

  deleteBudget(): void {
    if(this.budgets.length === 0) {
      this.toastr.error('There is not budget to delete');
      return;
    }
      const dialogRef = this.dialog.open(ConfirmDeleteDialogComponent, {
        width: '400px',
        panelClass: 'custom-dialog-container',
        data: {isBudget:true},
      });

      dialogRef.afterClosed().subscribe((result) => {
        if (result) {
          this.httpClient.delete(`${this.baseUrl}/api/v1/wealth-core/budget/delete`).subscribe({
            next: (response) => {
              this.toastr.success('Budget deleted successfully');
              this.loadBudgetData();
            }, error: (err) => {
              console.error('Failed to update budget:', err);
              this.toastr.error('Failed to delete budget');
              try {
                const errorObj = typeof err.error === 'string' ? JSON.parse(err.error) : err.error;
                this.toastr.error(errorObj.message);
              } catch (e) {
                console.error('Failed to parse error:', err.error);
              }
            },
          })
        }
      });
    }

  
  

  filterExpenses() {
    this.loadBudgetData();
  }

  resetFilters() {
    const today = new Date();
    this.selectedYear = today.getFullYear();
    this.selectedMonth = today.getMonth() + 1;
    this.selectedCategory = '';
    this.filterExpenses();
  }

}
