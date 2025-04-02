import { CommonModule } from '@angular/common';
import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
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
  selector: 'app-add-amount-goal',
  standalone: true,
  imports: [MatDialogModule, ReactiveFormsModule, CommonModule,FormsModule,
    MatInputModule,
    MatCheckboxModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatIconModule,],
  templateUrl: './add-amount-goal.component.html',
  styleUrl: './add-amount-goal.component.css'
})
export class AddAmountGoalComponent {

  amountForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<AddAmountGoalComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { id: number }
  ) {
    this.amountForm = this.fb.group({
      amount: [0, [Validators.required, Validators.min(1)]]
    });
  }

  onSave(): void {
    if (this.amountForm.valid) {
      const amount = this.amountForm.get('amount')?.value;
      this.dialogRef.close(amount);
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }


}
