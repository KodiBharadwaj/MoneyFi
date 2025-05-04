import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { FormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-add-income-dialog',
  templateUrl: './add-income-dialog.component.html',
  styleUrls: ['./add-income-dialog.component.scss'],
  standalone: true,
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
export class AddIncomeDialogComponent {
  incomeSource = {
    source: '',
    amount: '',
    date: new Date(),
    category: '',
    recurring: false,
  };

  dialogTitle: string;

  constructor(
    public dialogRef: MatDialogRef<AddIncomeDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    const dialogData = data || {};

    if (dialogData.isUpdate) {
      this.dialogTitle = 'Update Income';
      this.incomeSource = { ...dialogData };
    } else {
      this.dialogTitle = 'Add New Income';
      this.incomeSource = {
        source: '',
        amount: '',
        date: new Date(),
        category: '',
        recurring: false,
      };
    }
  }

  isValid(): boolean {
    return (
      this.incomeSource.source.trim() !== '' &&
      this.incomeSource.amount !== '' &&
      this.incomeSource.date !== null &&
      this.incomeSource.category.trim() !== ''
    );
  }

  onSave() {
    if (this.isValid()) {
      this.dialogRef.close(this.incomeSource);
    } else {
      alert('Please fill in all required fields before saving.');
    }
  }

  onCancel() {
    this.dialogRef.close();
  }
}
