
import { CommonModule } from '@angular/common';
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
import { AddIncomeDialogComponent } from '../add-income-dialog/add-income-dialog.component';
import { Category } from '../model/category-list';
import { CategoryService } from '../services/category.service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-add-income-dialog',
  templateUrl: './add-expense-dialog.component.html',
  styleUrls: ['./add-expense-dialog.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatInputModule,
    MatCheckboxModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatIconModule
  ]
})
export class AddExpenseDialogComponent {
  expenseSource = {
    amount: '',
    date: new Date(),
    categoryId: null as number | null,
    description:'',
    recurring: false,
  };

  categories: Category[] = [];
  private editCategoryName?: string;

  dialogTitle: string;
  flag : boolean = false;
  
  constructor(
    private categoryService: CategoryService,
    private toastr: ToastrService,
    public dialogRef: MatDialogRef<AddIncomeDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any 
  ) {
    const dialogData = data || {}; 

    if (dialogData.isUpdate) {
      this.flag = true;
      this.editCategoryName = dialogData.category;  
      this.dialogTitle = 'Update Expense'; 
      this.expenseSource = { ...dialogData }; 
      this.getExpenseCategories();
    } else {
      this.dialogTitle = 'Add New Expense'; 
      this.expenseSource =  {amount: '',
        date: new Date(),
        categoryId: null,
        description:'',
        recurring: false,}
        this.getExpenseCategories();
    }
  }

  today: Date = new Date();

  isValid(): boolean {
    return (
      this.expenseSource.amount !== '' &&
      this.expenseSource.date !== null &&
      this.expenseSource.categoryId !== null &&
      this.expenseSource.description.trim() !== ''
    );
  }

  capitalizeFirstLetter() {
    if (this.expenseSource.description) {
      const trimmed = this.expenseSource.description.trim();
      if (trimmed.length > 0) {
        this.expenseSource.description = 
          trimmed.charAt(0).toUpperCase() + trimmed.slice(1);
      }
    }
  }

  getExpenseCategories(): void {
    this.categoryService.getExpenseCategories()
      .subscribe({
        next: (categories) => {
          this.categories = categories;

          // 👇 map category name to ID in edit mode
          if (this.flag && this.editCategoryName) {
            const matched = this.categories.find(
              c => c.category === this.editCategoryName
            );

            if (matched) {
              this.expenseSource.categoryId = matched.categoryId;
            }
          }
        },
        error: () => {
          this.toastr.error('Failed to load expense categories');
        }
      });
  }
  

  onSave() {
    if (this.isValid()) {
      this.dialogRef.close(this.expenseSource);
    } else {
      alert('Please fill in all required fields before saving.');
    }
  }

  onCancel() {
    this.dialogRef.close();
  }
}
