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
      this.http.get(`${this.baseUrl}/api/v1/admin/admin-requests/${this.data.username}/${this.enteredRefNumber}/${this.data.requestType}`).subscribe({
        next: () => 
          this.dialogRef.close('approved'),
        error: () => alert('Failed to approve')
      });
    } else {
      alert('Reference number does not match!');
    }
  }
}
