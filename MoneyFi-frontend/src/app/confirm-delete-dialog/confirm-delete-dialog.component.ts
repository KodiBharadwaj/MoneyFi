import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ConfirmLogoutDialogComponent } from '../confirm-logout-dialog/confirm-logout-dialog.component';

@Component({
  selector: 'app-confirm-delete-dialog',
  standalone: true,
  imports: [],
  templateUrl: './confirm-delete-dialog.component.html',
  styleUrl: './confirm-delete-dialog.component.scss'
})
export class ConfirmDeleteDialogComponent {

  name : String = "";

  constructor(private dialogRef: MatDialogRef<ConfirmLogoutDialogComponent>, @Inject(MAT_DIALOG_DATA) public data: any) {
    if(data.isIncome){
      this.name = data.source;
    }
    else if(data.isExpense){
      this.name = data.description + " (" + data.category + ")";
    }
    else if(data.isGoal){
      this.name = data.goalName + " (" + data.category + ")";
    } else if(data.isBudget){
      this.name = "Budget";
    }
    else {
      this.name = "";
    }
  }

  confirmDelete(): void {
    this.dialogRef.close(true);
  }

  cancelDelete(): void {
    this.dialogRef.close(false);
  }
}
