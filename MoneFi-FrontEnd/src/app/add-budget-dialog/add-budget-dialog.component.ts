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
  baseUrl = "http://localhost:8765";

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
    'Miscellaneous'
  ];

  constructor(
    public dialogRef: MatDialogRef<AddBudgetDialogComponent>,@Inject(MAT_DIALOG_DATA) public data: any ,
    private httpClient: HttpClient
  ) {}


  
  ngOnInit() {
    const token = sessionStorage.getItem('finance.auth');
    
    // Get current month and year
    const currentDate = new Date();
    const month = currentDate.getMonth() + 1; 
    const year = currentDate.getFullYear();
    console.log(month);
    console.log(year);
  
    if (token) {
      this.httpClient.get<number>(`${this.baseUrl}/auth/token/${token}`).subscribe({
        next: (userId) => {
          this.httpClient.get<number>(`${this.baseUrl}/api/user/${userId}/totalIncome/${month}/${year}`).subscribe({
            next: (totalIncome) => {
              this.totalIncome = totalIncome;
              this.initializeCategories();
            },
          });
        },
      });
    }
  }
  

  // initializeCategories() {
  //   // Generate random percentages that sum up to 100
  //   const randomPercentages = this.generateRandomPercentages(this.categories.length);

  //   // Assign percentages and calculate initial amounts
  //   this.budgetSource.categories = this.categories.map((category, index) => ({
  //     category,
  //     percentage: randomPercentages[index],
  //     moneyLimit: 0,
  //   }));
  // }
  initializeCategories() {
    // Define fixed percentages for each category
    const fixedPercentages = [
      13, // Food
      7, // Travelling
      5,  // Entertainment
      12, // Groceries
      10, // Shopping
      10, // Bills & utilities
      20, // House Rent
      10,  // Emi and loans
      8,  // Health & Medical
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
  
  

  generateRandomPercentages(count: number): number[] {
    const percentages = Array.from({ length: count }, () => Math.random());
    const sum = percentages.reduce((acc, value) => acc + value, 0);

    // Normalize to make the sum 100
    return percentages.map((value) => Math.round((value / sum) * 100));
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

    // if (totalPercentage !== 100) {
    //   alert('An internal error occurred: Percentages do not sum to 100.');
    //   return;
    // }
    this.dialogRef.close(this.budgetSource);
  }

  onCancel() {
    this.dialogRef.close();
  }
}
