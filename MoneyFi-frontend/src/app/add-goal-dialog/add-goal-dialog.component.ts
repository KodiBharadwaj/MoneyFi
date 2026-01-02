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
import { Category } from '../model/category-list';
import { CategoryService } from '../services/category.service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-add-goal-dialog',
  standalone: true,
  templateUrl: './add-goal-dialog.component.html',
  styleUrl: './add-goal-dialog.component.scss',
  imports: [
    FormsModule,
    MatInputModule,
    MatCheckboxModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatIconModule,
    CommonModule
  ]
})
export class AddGoalDialogComponent {
  goalSource = {
    goalName: '',
    currentAmount: '',
    targetAmount: '',
    deadLine: new Date(),
    categoryId: null as number | null,
    description: '',
  };

  dialogTitle: string;
  updateGoal : boolean = false;
  categories: Category[] = [];
  flag : boolean = false;
  private editCategoryName?: string;

  constructor(
    private categoryService: CategoryService,
    private toastr:ToastrService,
    public dialogRef: MatDialogRef<AddGoalDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    const dialogData = data || {};
  
    if (dialogData.isUpdate) {
      this.editCategoryName = dialogData.category; 
      this.updateGoal = true;
      this.dialogTitle = 'Update Goal';
      this.goalSource = { ...dialogData };
      this.flag = true;
      this.getGoalCategories();
    } else {
      this.dialogTitle = 'Add New Goal';
      this.goalSource = {
        goalName: '',
        currentAmount: '',
        targetAmount: '',
        deadLine: new Date(),
        categoryId: null,
        description: '',
      };
      this.getGoalCategories();
    }
  }

  today: Date = new Date();

  isValid(): boolean {
    return (
      this.goalSource.goalName.trim() !== '' &&
      this.goalSource.currentAmount !== '' &&
      this.goalSource.targetAmount !== '' &&
      this.goalSource.deadLine !== null &&
      this.goalSource.categoryId !== null
    );
  }

  getGoalCategories(): void {
    this.categoryService.getGoalCategories()
      .subscribe({
        next: (categories) => {
          this.categories = categories;

          if (this.flag && this.editCategoryName) {
            const matched = this.categories.find(
              c => c.category === this.editCategoryName
            );
            if (matched) {
              this.goalSource.categoryId = matched.categoryId;
            }
          }
        },
        error: () => {
          this.toastr.error('Failed to load goal categories');
        }
      });
  }

  capitalizeFirstLetter() {
    if (this.goalSource.goalName && this.goalSource.goalName.length > 0) {
      this.goalSource.goalName = this.goalSource.goalName.charAt(0).toUpperCase() + this.goalSource.goalName.slice(1);
    }
  }

  onSave() {
    if (this.isValid()) {
      this.dialogRef.close(this.goalSource);
    } else {
      alert('Please fill in all required fields before saving.');
    }
  }

  onCancel() {
    this.dialogRef.close();
  }
}