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
import { Category } from '../model/category-list';
import { CategoryService } from '../services/category.service';

interface BudgetCategory {
  categoryId: number;
  categoryName: string;
  percentage: number;
  moneyLimit: number;
}

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
    categories: [] as BudgetCategory[],
  };


  totalIncome: number = 0;

  categories: Category[] = [];

  constructor(
    public dialogRef: MatDialogRef<AddBudgetDialogComponent>,@Inject(MAT_DIALOG_DATA) public data: any ,
    private httpClient: HttpClient,
    private categoryService: CategoryService
  ) {}


  
  ngOnInit() {
    this.categoryService.getExpenseCategories().subscribe(data => this.categories = data);
    
    // Get current month and year
    const currentDate = new Date();
    const month = currentDate.getMonth() + 1; 
    const year = currentDate.getFullYear();
  
    this.httpClient.get<number>(`${this.baseUrl}/api/v1/transaction/income/totalIncome/${month}/${year}`).subscribe({
      next: (totalIncome) => {
        this.totalIncome = totalIncome;
        this.initializeCategories();
      },
    });
  }
  
  initializeCategories() {
    const fixedPercentages = [
      13, // Food
      7,  // Travelling
      5,  // Entertainment
      8,  // Groceries
      10, // Shopping
      10, // Bills & utilities
      10, // House Rent
      6,  // Emi & loans
      8,  // Health & Medical
      18, // Goal
      5   // Miscellaneous
    ];

    const totalPercentage = fixedPercentages.reduce((a, b) => a + b, 0);
    if (totalPercentage !== 100) {
      throw new Error('Percentages must sum to 100');
    }

    this.budgetSource.categories = this.categories.map(
      (cat, index): BudgetCategory => ({
        categoryId: cat.categoryId,
        categoryName: cat.category,
        percentage: fixedPercentages[index] ?? 0,
        moneyLimit: 0,
      })
    );
  }

  onBudgetChange() {
    const totalBudget = this.budgetSource.moneyLimit;

    this.budgetSource.categories.forEach((category: BudgetCategory) => {
      category.moneyLimit = (totalBudget * category.percentage) / 100;
    });
  }

  onSave() {
    const totalBudget = this.budgetSource.moneyLimit;

    if (totalBudget > this.totalIncome) {
      alert(
        `The total budget cannot exceed your total income of ₹${this.totalIncome}`
      );
      return;
    }

    const totalPercentage = this.budgetSource.categories.reduce(
      (sum: number, category: BudgetCategory) => sum + category.percentage,
      0
    );

    if (totalPercentage !== 100) {
      alert('Category percentages must total 100%');
      return;
    }

    // ✅ Send only what backend needs
    const payload = this.budgetSource.categories.map(c => ({
      categoryId: c.categoryId,
      percentage: c.percentage,
      moneyLimit: c.moneyLimit,
    }));

    this.dialogRef.close(payload);
  }

  onCancel() {
    this.dialogRef.close();
  }
}
