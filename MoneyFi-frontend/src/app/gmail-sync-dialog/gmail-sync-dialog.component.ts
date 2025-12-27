import { Component, Inject, NgModule, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { HttpClient } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';
import { ParsedTransaction } from '../model/ParsedTransaction';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-gmail-sync-dialog',
  standalone: true,
  imports: [CommonModule,
    FormsModule,
    MatDialogModule,
    MatProgressSpinnerModule,
    MatButtonModule],
  templateUrl: './gmail-sync-dialog.component.html',
  styleUrl: './gmail-sync-dialog.component.css'
})
export class GmailSyncDialogComponent implements OnInit {

  loading = true;
  transactions: ParsedTransaction[] = [];
  remainingCount = 0;

  BASE_URL = environment.BASE_URL;

  transactionTypes = ['CREDIT', 'DEBIT', 'CREDIT OR DEBIT'];

  constructor(
    @Inject(MAT_DIALOG_DATA) private data: { code: string },
    private http: HttpClient,
    private dialogRef: MatDialogRef<GmailSyncDialogComponent>,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.startSync();
  }

  startSync() {
  this.loading = true;

  this.http
    .post<Record<number, ParsedTransaction[]>>(
      `${this.BASE_URL}/api/v1/gmail-sync/enable`,
      { code: this.data.code }
    )
    .subscribe({
      next: (res) => {
        // Extract count (map key)
        const [countKey] = Object.keys(res);
        this.remainingCount = Number(countKey);

        // Extract transaction list
        const txList = res[this.remainingCount] ?? [];

        this.transactions = txList.map(tx => ({
          ...tx,
          accepted: true,
        }));

        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        try {
          const errorObj =
            typeof err.error === 'string' ? JSON.parse(err.error) : err.error;
          this.toastr.error(errorObj.message);
        } catch {
          console.error('Failed to parse error:', err.error);
        }
      },
    });
}


  ignore(index: number) {
    this.transactions.splice(index, 1);
  }

  cancel() {
    this.dialogRef.close();
  }

  
  submitting = false;

  submit() {
    const accepted = this.transactions.filter(t => t.accepted);

    if (accepted.length === 0) {
      return;
    }

    this.submitting = true;

    this.http
      .post(`${this.BASE_URL}/api/v1/transaction/gmail-sync/bulk-save`, accepted)
      .subscribe({
        next: () => {
          this.toastr.success('Transactions added successfully');
          this.dialogRef.close(true);
        },
        error: () => {
          this.toastr.error('Failed to add transactions');
          this.submitting = false;
        },
        complete: () => {
          this.submitting = false;
        },
      });
  }

}
