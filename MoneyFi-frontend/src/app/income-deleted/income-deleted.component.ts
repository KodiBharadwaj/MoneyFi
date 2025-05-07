import { CommonModule } from '@angular/common';
import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-income-deleted',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './income-deleted.component.html',
  styleUrl: './income-deleted.component.css'
})
export class IncomeDeletedComponent {

  constructor(public dialogRef: MatDialogRef<IncomeDeletedComponent>, 
    @Inject(MAT_DIALOG_DATA) public data: { deletedIncomes: any[] }) {}

  closeDialog(): void {
    // You can add cleanup logic here if needed
    this.dialogRef.close();
  }
}
