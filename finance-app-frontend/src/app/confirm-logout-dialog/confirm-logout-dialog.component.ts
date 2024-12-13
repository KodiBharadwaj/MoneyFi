import { Component } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-confirm-logout-dialog',
  templateUrl: './confirm-logout-dialog.component.html',
  styleUrls: ['./confirm-logout-dialog.component.scss'],
  standalone:true,
})
export class ConfirmLogoutDialogComponent {
  constructor(private dialogRef: MatDialogRef<ConfirmLogoutDialogComponent>) {}

  confirmLogout(): void {
    this.dialogRef.close(true);
  }

  cancelLogout(): void {
    this.dialogRef.close(false);
  }
}
