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
  analysisFromDate: string = ''; 
  analysisToDate: string = '';
  
  // Pagination properties
  startIndex: number = 0;
  threshold: number = 10;
  currentPage: number = 1;

  // Loading states for buttons
  isGenerating: boolean = false;
  isGeneratingForSpendingAnalysis: boolean = false;
  isDownloading: boolean = false;
  isDownloadingForSpendingAnalysis: boolean = false;
  isSendingEmail: boolean = false;
  isSendingEmailForSpendingAnalysis: boolean = false;

 ngOnInit() {
  const today = new Date();
  const sevenDaysAgo = new Date();
  sevenDaysAgo.setDate(today.getDate() - 7);

  this.toDate = today.toISOString().split('T')[0];      // Format: YYYY-MM-DD
  this.fromDate = sevenDaysAgo.toISOString().split('T')[0]; // Format: YYYY-MM-DD
  this.analysisToDate = today.toISOString().split('T')[0];      // Format: YYYY-MM-DD
  this.analysisFromDate = sevenDaysAgo.toISOString().split('T')[0]; // Format: YYYY-MM-DD
}

  constructor(private httpClient : HttpClient, private toastr:ToastrService) {}
  accountStatementGenerated: any[] = [];

  baseUrl = environment.BASE_URL;

  generateStatement(): void {
    if (!this.fromDate || !this.toDate) {
      this.toastr.warning('Please provide date range');
      return;
    }

    const from = new Date(this.fromDate);
    const to = new Date(this.toDate);

    if (from > to) {
        this.toastr.warning('From date should be before End Date');
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

    this.httpClient.post<AccountStatement[]>(`${this.baseUrl}/api/v1/transaction/account-statement`, obj)
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

    this.httpClient.post(`${this.baseUrl}/api/v1/transaction/account-statement/report`, obj, {
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

    this.httpClient.post(`${this.baseUrl}/api/v1/transaction/account-statement-report/email`, obj, { responseType: 'text' })
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


  spendingAnalysis: any = null;
  incomeCategories: { key: string, value: number }[] = [];
  expenseCategories: { key: string, value: number }[] = [];

  getSpendingAnalysis(): void {
    if (!this.analysisFromDate || !this.analysisToDate) {
      this.toastr.warning('Please provide date range');
      return;
    }

    const from = new Date(this.analysisFromDate);
    const to = new Date(this.analysisToDate);

    if (from > to) {
        this.toastr.warning('From date should be before End Date');
        return;
    }
    
    this.isGeneratingForSpendingAnalysis = true;
    this.fetchSpendingAnalysis(from, to);
  }

  private fetchSpendingAnalysis(fromDate : Date, toDate : Date): void {
    const formattedFromDate = fromDate.toISOString().split('T')[0]; // yyyy-MM-dd
    const formattedToDate = toDate.toISOString().split('T')[0];     // yyyy-MM-dd

    this.httpClient.get<any>(
      `${this.baseUrl}/api/v1/wealth-core/spending-analysis?fromDate=${formattedFromDate}&toDate=${formattedToDate}`
    ).subscribe({
      next: (analysis) => {
        console.log(analysis);
        this.spendingAnalysis = analysis;
        this.incomeCategories = Object.entries(analysis.incomeByCategory)
          .map(([key, value]) => ({ key, value: Number(value) }));
        this.expenseCategories = Object.entries(analysis.expenseByCategory)
          .map(([key, value]) => ({ key, value: Number(value) }));

        this.isGeneratingForSpendingAnalysis = false;
      },error: (error) => {
        this.toastr.error('Error fetching spending analysis');
        console.error('Error fetching spending analysis:', error);
        this.isGeneratingForSpendingAnalysis = false;
      }
    });
  }

  downloadSpendingAnalysis(){
    this.isDownloadingForSpendingAnalysis = true;
    const from = new Date(this.analysisFromDate);
    const to = new Date(this.analysisToDate);
    const formattedFromDate = from.toISOString().split('T')[0]; // yyyy-MM-dd
    const formattedToDate = to.toISOString().split('T')[0];     // yyyy-MM-dd
    this.httpClient.get(`${this.baseUrl}/api/v1/wealth-core/spending-analysis/report?fromDate=${formattedFromDate}&toDate=${formattedToDate}`, {
      responseType: 'blob'
    }).subscribe({
      next: (blob) => {
        const fileURL = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = fileURL;
        a.download = 'spending-analysis.pdf';
        a.click();
        URL.revokeObjectURL(fileURL);
        this.isDownloadingForSpendingAnalysis = false;
      },
      error: (error) => {
        console.error('Error downloading spending analysis:', error);
        this.toastr.error('Error downloading spending analysis');
        this.isDownloadingForSpendingAnalysis = false;
      }
    });
  }

  sendSpendingAnalysisEmail(){
    this.isSendingEmailForSpendingAnalysis = true;
    const from = new Date(this.analysisFromDate);
    const to = new Date(this.analysisToDate);
    const formattedFromDate = from.toISOString().split('T')[0]; // yyyy-MM-dd
    const formattedToDate = to.toISOString().split('T')[0];     // yyyy-MM-dd
    this.httpClient.get(`${this.baseUrl}/api/v1/wealth-core/spending-analysis/report-email?fromDate=${formattedFromDate}&toDate=${formattedToDate}`, { responseType: 'text' })
    .subscribe({
      next: (response: string) => {
        if (response === 'Email sent successfully') {
          this.toastr.success(response);
        } else {
          this.toastr.error("Failed to send email, Please try later")
        }
        this.isSendingEmailForSpendingAnalysis = false;
      },
      error: (error) => {
        console.error('Error occurred:', error);
        this.toastr.error("Failed to send email, Please try later");
        this.isSendingEmailForSpendingAnalysis = false;
      }
    });
  }
}