import { HttpClient } from '@angular/common/http';
import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { environment } from '../../environments/environment';
import { FormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-admin-request-decline-dialog',
  standalone: true,
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
  templateUrl: './admin-request-decline-dialog.component.html',
  styleUrl: './admin-request-decline-dialog.component.css'
})
export class AdminRequestDeclineDialogComponent {
enteredRefNumber: string = '';
declineReasonInput : string = '';

  constructor(
    public dialogRef: MatDialogRef<AdminRequestDeclineDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private http: HttpClient
  ) {}

  baseUrl = environment.BASE_URL;

  onCancel(): void {
    this.dialogRef.close();
  }

  onConfirm(): void {
    if (this.enteredRefNumber === this.data.referenceNumber) {
      const dto = {
        email : this.data.username,
        referenceNumber : this.enteredRefNumber,
        requestStatus : this.data.requestType,
        declineReason : this.declineReasonInput,
        approveStatus : 'Decline'
      }
      this.http.post(`${this.baseUrl}/api/v1/admin/user-requests/action`, dto).subscribe({
        next : () => {
          this.dialogRef.close('declined');
        },
        error : () => alert('Failed to decline')
      })
    } else {
      alert('Reference number does not match!');
    }
  }
}
