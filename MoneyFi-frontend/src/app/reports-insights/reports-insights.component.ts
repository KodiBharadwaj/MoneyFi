import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { environment } from '../../environments/environment';
import { CommonModule } from '@angular/common';
import { ToastrService } from 'ngx-toastr';
import { FormsModule } from '@angular/forms';

interface accountStatement {
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
  constructor(private httpClient : HttpClient, private toastr:ToastrService) {}
  accountStatementGenerated: any[] = [];

  baseUrl = environment.BASE_URL;

  generateStatement(){
    this.httpClient.get<accountStatement[]>(`${this.baseUrl}/api/v1/income/account-statement/${this.fromDate}/${this.toDate}`).subscribe({
      next : (statement) => {
        this.accountStatementGenerated = statement;
      }
    })
  }

  downloadStatement(){

    this.httpClient.get(`${this.baseUrl}/api/v1/income/account-statement/report/${this.fromDate}/${this.toDate}`, {
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

    this.httpClient.get(`${this.baseUrl}/api/v1/income/account-statement-report/email/${this.fromDate}/${this.toDate}`, { responseType: 'text' })
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
