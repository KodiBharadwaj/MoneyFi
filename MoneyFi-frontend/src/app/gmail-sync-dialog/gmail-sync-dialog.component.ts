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
    this.http
      .post<ParsedTransaction[]>(
        `${this.BASE_URL}/api/v1/gmail-sync/enable`,
        { code: this.data.code }
      )
      .subscribe({
        next: (res) => {
          this.transactions = res.map(tx => ({ ...tx, accepted: true }));
          this.loading = false;
        },
        error: () => {
          this.toastr.error('Failed to sync transactions');
          this.dialogRef.close();
        },
      });
  }

//   startSync() {
//   const dummyData: ParsedTransaction[] = [
//     {
//       category: 'others',
//       description: 'UPI transaction',
//       amount: 1200,
//       transactionType: 'DEBIT',
//       transactionDate: '2025-12-26T17:39:05',
//     },
//     {
//       category: 'Bills & utilities',
//       description: 'UPI transaction',
//       amount: 253.0,
//       transactionType: 'CREDIT',
//       transactionDate: '2025-12-26T17:31:45',
//     },
//     {
//       category: 'Travelling',
//       description: 'Card transaction',
//       amount: 56698.0,
//       transactionType: 'CREDIT OR DEBIT',
//       transactionDate: '2025-12-26T17:31:45',
//     },
//   ];

//   this.transactions = dummyData.map(tx => ({
//     ...tx,
//     accepted: true,
//   }));

//   this.loading = false;
// }


  ignore(index: number) {
    this.transactions.splice(index, 1);
  }

  cancel() {
    this.dialogRef.close();
  }

  submit() {
    const accepted = this.transactions.filter(t => t.accepted);

    this.http.post(`${this.BASE_URL}/api/v1/transaction/gmail-sync/bulk-save`, accepted)
      .subscribe(() => {
        this.toastr.success('Transactions added successfully');
        this.dialogRef.close(true);
      });
  }
}
