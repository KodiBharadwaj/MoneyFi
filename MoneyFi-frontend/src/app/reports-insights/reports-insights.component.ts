import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { environment } from '../../environments/environment';
import { CommonModule } from '@angular/common';
import { ToastrService } from 'ngx-toastr';
import { FormsModule } from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatNativeDateModule } from '@angular/material/core';

export interface AccountStatement {
  id: number;
  transactionDate: Date;
  transactionTime:string;
  description: string;
  amount: number;
  creditOrDebit: string;
  totalExpenses : number;
}

@Component({
  selector: 'app-reports-insights',
  standalone: true,
  imports: [CommonModule, FormsModule, MatDatepickerModule, MatFormFieldModule, MatInputModule, MatNativeDateModule, FormsModule],
  templateUrl: './reports-insights.component.html',
  styleUrl: './reports-insights.component.css'
})
export class ReportsInsightsComponent implements OnInit   {

  fromDate: string = ''; 
  toDate: string = '';
  
  // Pagination properties
  startIndex: number = 0;
  threshold: number = 10;
  currentPage: number = 1;

  // Loading states for buttons
  isGenerating: boolean = false;
  isDownloading: boolean = false;
  isSendingEmail: boolean = false;

 ngOnInit() {
  const today = new Date();
  const sevenDaysAgo = new Date();
  sevenDaysAgo.setDate(today.getDate() - 7);

  this.toDate = today.toISOString().split('T')[0];      // Format: YYYY-MM-DD
  this.fromDate = sevenDaysAgo.toISOString().split('T')[0]; // Format: YYYY-MM-DD
}

  constructor(private httpClient : HttpClient, private toastr:ToastrService) {}
  accountStatementGenerated: any[] = [];

  baseUrl = environment.BASE_URL;

  generateStatement(): void {
    if (!this.fromDate || !this.toDate) {
      this.toastr.warning('Please provide date range');
      return;
    }
    
    // Reset pagination when generating new statement
    this.startIndex = 0;
    this.currentPage = 1;
    
    this.isGenerating = true;
    this.fetchStatements();
  }

  private fetchStatements(): void {
    const obj = {
      fromDate: this.fromDate,
      toDate: this.toDate,
      startIndex: this.startIndex,
      threshold: this.threshold
    };

    this.httpClient.post<AccountStatement[]>(`${this.baseUrl}/api/v1/income/account-statement`, obj)
      .subscribe({
        next: (statement) => {
          if(statement.length === 0)
            this.toastr.warning('No transactions found in this range')
          this.accountStatementGenerated = statement;
          this.isGenerating = false;
        },
        error: (error) => {
          this.toastr.error('Error fetching account statements');
          console.error('Error fetching statements:', error);
          this.isGenerating = false;
        }
      });
  }

  /**
   * Go to next page
   */
  goToNextPage(): void {
    if (this.accountStatementGenerated.length === this.threshold) {
      this.startIndex += this.threshold;
      this.currentPage++;
      this.fetchStatements();
    }
  }
  
  /**
   * Go to previous page
   */
  goToPreviousPage(): void {
    if (this.currentPage > 1) {
      this.startIndex = Math.max(0, this.startIndex - this.threshold);
      this.currentPage--;
      this.fetchStatements();
    }
  }

  downloadStatement(){
    this.isDownloading = true;
    
    const obj = {
      fromDate : this.fromDate,
      toDate : this.toDate,
      startIndex : this.startIndex,
      threshold : this.threshold
    };

    this.httpClient.post(`${this.baseUrl}/api/v1/income/account-statement/report`, obj, {
      responseType: 'blob'
    }).subscribe({
      next: (blob) => {
        const fileURL = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = fileURL;
        a.download = 'account-statement.pdf';
        a.click();
        URL.revokeObjectURL(fileURL);
        this.isDownloading = false;
      },
      error: (error) => {
        console.error('Error downloading statement:', error);
        this.toastr.error('Error downloading statement');
        this.isDownloading = false;
      }
    });
  }

  sendStatementEmail(){
    this.isSendingEmail = true;
    
    const obj = {
      fromDate : this.fromDate,
      toDate : this.toDate,
      startIndex : this.startIndex,
      threshold : this.threshold
    };

    this.httpClient.post(`${this.baseUrl}/api/v1/income/account-statement-report/email`, obj, { responseType: 'text' })
    .subscribe({
      next: (response: string) => {
        if (response === 'Email sent successfully') {
          this.toastr.success(response);
        } else {
          this.toastr.error("Failed to send email, Please try later")
        }
        this.isSendingEmail = false;
      },
      error: (error) => {
        console.error('Error occurred:', error);
        this.toastr.error("Failed to send email, Please try later");
        this.isSendingEmail = false;
      }
    });
  }
}