import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, Inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-add-budget-dialog',
  standalone: true,
  templateUrl: './add-budget-dialog.component.html',
  styleUrl: './add-budget-dialog.component.css',
    imports: [FormsModule,
    MatInputModule,
    MatCheckboxModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatIconModule,
  CommonModule],
})
export class AddBudgetDialogComponent {
  baseUrl = environment.BASE_URL;

  budgetSource = {
    moneyLimit: 0,
    categories: [] as { category: string; percentage: number; moneyLimit: number }[],
  };

  totalIncome: number = 0;

  categories = [
    'Food',
    'Travelling',
    'Entertainment',
    'Groceries',
    'Shopping',
    'Bills & utilities',
    'House Rent',
    'Emi and loans',
    'Health & Medical',
    'Goal',
    'Miscellaneous'
  ];

  constructor(
    public dialogRef: MatDialogRef<AddBudgetDialogComponent>,@Inject(MAT_DIALOG_DATA) public data: any ,
    private httpClient: HttpClient
  ) {}


  
  ngOnInit() {
    
    // Get current month and year
    const currentDate = new Date();
    const month = currentDate.getMonth() + 1; 
    const year = currentDate.getFullYear();
  
    this.httpClient.get<number>(`${this.baseUrl}/api/v1/transaction/income/user/totalIncome/${month}/${year}`).subscribe({
      next: (totalIncome) => {
        this.totalIncome = totalIncome;
        this.initializeCategories();
      },
    });
  }
  
  initializeCategories() {
    // Define fixed percentages for each category
    const fixedPercentages = [
      13, // Food
      7, // Travelling
      5,  // Entertainment
      8, // Groceries
      10, // Shopping
      10, // Bills & utilities
      10, // House Rent
      6,  // Emi and loans
      8,  // Health & Medical
      18, // Goal
      5   // Miscellaneous
    ];
  
    // Validate that the total percentage sums to 100
    const totalPercentage = fixedPercentages.reduce((sum, percentage) => sum + percentage, 0);
    if (totalPercentage !== 100) {
      throw new Error('Fixed percentages do not sum up to 100. Please adjust the values.');
    }
  
    // Assign percentages and initialize money limits
    this.budgetSource.categories = this.categories.map((category, index) => ({
      category,
      percentage: fixedPercentages[index],
      moneyLimit: 0, // Initialize moneyLimit to 0
    }));
  }

  onBudgetChange() {
    const totalBudget = this.budgetSource.moneyLimit;
    this.budgetSource.categories.forEach((category) => {
      category.moneyLimit = (totalBudget * category.percentage) / 100;
    });
  }

  onSave() {

    const totalBudget = this.budgetSource.moneyLimit;

    if (totalBudget > this.totalIncome) {
      alert(`The total budget cannot exceed your total income of ₹${this.totalIncome}. Please adjust your budget.`);
      return; // Prevent saving the budget
    }
    const totalPercentage = this.budgetSource.categories.reduce(
      (sum, category) => sum + category.percentage,
      0
    );
    
    this.dialogRef.close(this.budgetSource.categories);
  }

  onCancel() {
    this.dialogRef.close();
  }
}
