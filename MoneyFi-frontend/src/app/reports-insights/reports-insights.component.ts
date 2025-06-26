import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { environment } from '../../environments/environment';
import { CommonModule } from '@angular/common';
import { ToastrService } from 'ngx-toastr';
import { FormsModule } from '@angular/forms';

interface accountStatement {
  id : number;
  transactionDate : Date;
  description : string;
  amount : number;
  totalExpenses : number;
  creditOrDebit : string;
}

@Component({
  selector: 'app-reports-insights',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reports-insights.component.html',
  styleUrl: './reports-insights.component.css'
})
export class ReportsInsightsComponent {

  fromDate: string = ''; 
  toDate: string = '';
  startIndex : number = 0;
  threshold : number = -1;

  constructor(private httpClient : HttpClient, private toastr:ToastrService) {}
  accountStatementGenerated: any[] = [];

  baseUrl = environment.BASE_URL;

  generateStatement(){
    const obj = {
      fromDate : this.fromDate,
      toDate : this.toDate,
      startIndex : this.startIndex,
      threshold : this.threshold
    };

    this.httpClient.post<accountStatement[]>(`${this.baseUrl}/api/v1/income/account-statement`, obj).subscribe({
      next : (statement) => {
        this.accountStatementGenerated = statement;
      }
    })
  }

  downloadStatement(){
    const obj = {
      fromDate : this.fromDate,
      toDate : this.toDate,
      startIndex : this.startIndex,
      threshold : this.threshold
    };

    this.httpClient.post(`${this.baseUrl}/api/v1/income/account-statement/report`, obj, {
      responseType: 'blob'
    }).subscribe(blob => {
        const fileURL = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = fileURL;
        a.download = 'account-statement.pdf';
        a.click();
        URL.revokeObjectURL(fileURL);
    });
  }

  sendStatementEmail(){
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
          this.toastr.success("Email sent successfully");
        } else {
          this.toastr.error("Failed to send, Please try later")
        }
      },
      error: (error) => {
        console.error('Error occurred:', error);
        this.toastr.error("Failed to send, Please try later")
      }
    });
  }
}
