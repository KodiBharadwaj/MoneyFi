import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule, MatLabel } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatIconModule } from '@angular/material/icon';
import { environment } from '../../environments/environment';

interface AdminActionDto {
  email: string;
  referenceNumber: string;
  requestStatus: string;
  declineReason: string;
  approveStatus: string;
  gmailSyncRequestCount?: number | null; // optional field
}


@Component({
  selector: 'app-admin-request-dialog',
  templateUrl: './admin-request-dialog.component.html',
  styleUrls: ['./admin-request-dialog.component.css'],
  standalone : true,
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
})
export class AdminRequestDialogComponent {
  enteredRefNumber: string = '';
  adminCount: number | null = null; // new field

  constructor(
    public dialogRef: MatDialogRef<AdminRequestDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private http: HttpClient
  ) {}

  baseUrl = environment.BASE_URL;

  onCancel(): void {
    this.dialogRef.close();
  }

  onConfirm(): void {
    if (this.enteredRefNumber === this.data.referenceNumber) {
      const dto: AdminActionDto = {
      email: this.data.username,
      referenceNumber: this.enteredRefNumber,
      requestStatus: this.data.requestType,
      declineReason: '',
      approveStatus: 'Approve'
    };

    if (this.data.requestType === 'GMAIL_SYNC_REQUEST_TYPE') {
      dto.gmailSyncRequestCount = this.adminCount;
    }
      if (this.data.requestType === 'GMAIL_SYNC_REQUEST_TYPE' && !this.adminCount) {
        alert('Please enter count');
        return;
      }
      this.http.post(`${this.baseUrl}/api/v1/user-service/admin/user-requests/action`, dto).subscribe({
        next : () => {
          this.dialogRef.close('approved');
        },
        error : () => alert('Failed to approve')
      })
    } else {
      alert('Reference number does not match!');
    }
  }
}
