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
    category: '',
  };

  dialogTitle: string;

  constructor(
    public dialogRef: MatDialogRef<AddGoalDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    const dialogData = data || {};

    if (dialogData.isUpdate) {
      this.dialogTitle = 'Update Goal';
      this.goalSource = { ...dialogData };
    } else {
      this.dialogTitle = 'Add New Goal';
      this.goalSource = {
        goalName: '',
        currentAmount: '',
        targetAmount: '',
        deadLine: new Date(),
        category: '',
      };
    }
  }

  isValid(): boolean {
    return (
      this.goalSource.goalName.trim() !== '' &&
      this.goalSource.currentAmount !== '' &&
      this.goalSource.targetAmount !== '' &&
      this.goalSource.deadLine !== null &&
      this.goalSource.category.trim() !== ''
    );
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