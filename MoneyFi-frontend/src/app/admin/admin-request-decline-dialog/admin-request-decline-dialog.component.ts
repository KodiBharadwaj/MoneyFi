import { HttpClient } from '@angular/common/http';
import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { environment } from '../../../environments/environment';
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
import { ToastrService } from 'ngx-toastr';

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
    private http: HttpClient,
    private toastr: ToastrService
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
        requestType : this.data.requestType,
        declineReason : this.declineReasonInput,
        approveStatus : 'Decline'
      }
      this.http.post(`${this.baseUrl}/api/v1/user-service/admin/user-requests/action`, dto).subscribe({
        next : () => {
          this.dialogRef.close('declined');
        },
          error: (err) => {
          console.error('Failed to load income data:', err);
          try {
              const errorObj = typeof err.error === 'string' ? JSON.parse(err.error) : err.error;
              const message = errorObj?.message || errorObj?.error || 'Something went wrong';
              this.toastr.error(message, 'Login Failed');
            } catch (e) {
              console.error('Failed to parse error:', err.error);
            }
        }
      })
    } else {
      alert('Reference number does not match!');
    }
  }
}
