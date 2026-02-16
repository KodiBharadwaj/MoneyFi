import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { MatDialog } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ToastrService } from 'ngx-toastr';
import { GmailSyncCalendarComponent } from '../gmail-sync-calendar/gmail-sync-calendar.component';
import { environment } from '../../environments/environment';
import { GmailSyncDialogComponent } from '../gmail-sync-dialog/gmail-sync-dialog.component';
import { FormsModule } from '@angular/forms';

declare const google: any;

interface GmailSyncHistoryResponse {
  syncTime: string;
  syncCount: number;
}

@Component({
  selector: 'app-gmail-sync-summary',
  standalone: true,
  imports: [CommonModule, MatProgressSpinnerModule, GmailSyncCalendarComponent, FormsModule],
  templateUrl: './gmail-sync-summary.component.html',
  styleUrl: './gmail-sync-summary.component.css'
})
export class GmailSyncSummaryComponent implements OnInit {

  constructor(
    private dialog: MatDialog,
    private httpClient: HttpClient,
    private toastr: ToastrService
  ) {}

  selectedDate: Date = new Date();
  syncHistory: GmailSyncHistoryResponse[] = [];
  transactions: any[] = [];
  loadingTransactions = false;
  isLoading = false;
  
  baseUrl = environment.BASE_URL;
  userServiceBaseUrl = environment.USER_SERVICE_URL;
  
  gmailSyncEnabled = false;
  isGmailConnected = false;

  ngOnInit(): void {
    this.checkGmailSyncStatus();
    this.loadHistory();
    this.fetchTransactions(new Date);
  }

  loadHistory() {
    this.httpClient
      .get<GmailSyncHistoryResponse[]>(
        `${this.baseUrl}/api/v1/gmail-sync/history`
      )
      .subscribe(res => {
        this.syncHistory = res;
      });
  }

  onDateSelected(date: Date) {
    this.selectedDate = date;
    
    // Check if this date has sync history
    const hasHistory = this.syncHistory.find(h =>
      new Date(h.syncTime).toDateString() === date.toDateString()
    );
    
    if (hasHistory) {
      // Date has history, fetch transactions from API
      this.fetchTransactions(date);
    } else {
      // Date has no history, directly show empty state without API call
      this.transactions = [];
      this.loadingTransactions = false;
    }
  }

  fetchTransactions(date: Date) {
    this.loadingTransactions = true;
    // Format date in local timezone to avoid timezone offset issues
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const formattedDate = `${year}-${month}-${day}`;

    this.httpClient
      .get<any>(
        `${this.baseUrl}/api/v1/transaction/gmail-sync-transactions?date=${formattedDate}`
      )
      .subscribe({
        next: res => {
          this.transactions = [
            ...(res.incomes || []),
            ...(res.expenses || [])
          ];
          this.loadingTransactions = false;
        },
        error: () => {
          this.loadingTransactions = false;
        }
      });
  }

  checkGmailSyncStatus() {
    this.httpClient
      .get<number>(`${this.baseUrl}/api/v1/gmail-sync/status`)
      .subscribe((res) => {
        if(res === null) {
          this.isGmailConnected = false;
        } else this.isGmailConnected = true;
        this.gmailSyncEnabled = res >= 3;
      });
  }

  onGmailSyncClick() {
    if (this.isGmailConnected) {
      this.startSync();
    } else {
      this.startGoogleConsent();
    }
  }

  gmailSyncReConsent() {
    this.startGoogleConsent();
  }

  startGoogleConsent() {
    const client = google.accounts.oauth2.initCodeClient({
      client_id: environment.GOOGLE_CLIENT_ID,
      scope: 'openid email profile https://www.googleapis.com/auth/gmail.readonly',
      access_type: 'offline',
      prompt: 'consent',
      callback: (response: any) => this.handleGmailSync(response),
    });
    client.requestCode();
  }

  startSync() {
    // Format date in local timezone to avoid timezone offset issues
    const year = this.selectedDate.getFullYear();
    const month = String(this.selectedDate.getMonth() + 1).padStart(2, '0');
    const day = String(this.selectedDate.getDate()).padStart(2, '0');
    const formattedDate = `${year}-${month}-${day}`;
    
    const dialogRef = this.dialog.open(GmailSyncDialogComponent, {
      width: '80vw',
      maxHeight: '85vh',
      disableClose: true,
      backdropClass: 'gmail-sync-backdrop',
      data: { syncDate: formattedDate }
    });

    dialogRef.afterClosed().subscribe(() => {
      this.checkGmailSyncStatus();
      this.loadHistory();
      this.fetchTransactions(this.selectedDate);
    });
  }

  handleGmailSync(response: any) {
    this.httpClient
      .post(`${this.baseUrl}/api/v1/gmail-sync/enable`, { code: response.code })
      .subscribe({
        next: () => {
          console.log('Gmail sync enabled');
          this.isGmailConnected = true;
          this.startSync();
        },
        error: (err) => {
          console.error('Failed to enable Gmail sync', err);
          try {
            const errorObj = typeof err.error === 'string' ? JSON.parse(err.error) : err.error;
            this.toastr.error(errorObj.message);
          } catch (e) {
            console.error('Failed to parse error:', err.error);
            this.toastr.error('Failed to sync Gmail Transactions');
          }
        }
      });
  }

  showGmailSyncRequestModal = false;
  isSubmitting = false;

  gmailSyncRequest = {
    count: null,
    reason: ''
  };

  selectedImage: File | null = null;

  openGmailSyncRequestModal() {
    this.showGmailSyncRequestModal = true;
  }

  closeGmailSyncRequestModal() {
    this.showGmailSyncRequestModal = false;
    this.gmailSyncRequest = { count: null, reason: '' };
    this.selectedImage = null;
  }

  onImageSelected(event: any) {
    if (event.target.files && event.target.files.length > 0) {
      this.selectedImage = event.target.files[0];
    }
  }

  submitGmailSyncRequest() {
    if (!this.gmailSyncRequest.count || !this.gmailSyncRequest.reason) return;

    this.isSubmitting = true;

    const formData = new FormData();

    const data = {
      count: this.gmailSyncRequest.count,
      reason: this.gmailSyncRequest.reason
    };

    formData.append(
      'data',
      new Blob([JSON.stringify(data)], { type: 'application/json' })
    );

    if (this.selectedImage) {
      formData.append('image', this.selectedImage);
    }

    fetch(`${this.baseUrl}/api/v1/user-service/user/user-request/gmail-sync-request`, {
      method: 'POST',
      headers: {
        Authorization: 'Bearer ' + sessionStorage.getItem('moneyfi.auth')
      },
      body: formData
    })
      .then(async (response) => {
        this.isSubmitting = false;

        if (!response.ok) {
          // Handle backend error (400, 401, 500, etc.)
          let errorMessage = 'Failed to submit request';

          try {
            const errorObj = await response.json();
            errorMessage = errorObj.message || errorMessage;
          } catch (e) {
            // If backend didn't return JSON
            errorMessage = `Error: ${response.status}`;
          }

          this.toastr.error(errorMessage);
          return;
        }

        // Success
        this.closeGmailSyncRequestModal();
        this.toastr.success('Request sent to admin successfully!');
      })
      .catch((err) => {
        this.isSubmitting = false;

        // Only network-level errors come here
        console.error('Network error:', err);
        this.toastr.error('Network error. Please check your connection.');
      });
  }

}