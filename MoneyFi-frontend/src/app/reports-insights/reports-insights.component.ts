import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-reports-insights',
  standalone: true,
  imports: [],
  templateUrl: './reports-insights.component.html',
  styleUrl: './reports-insights.component.css'
})
export class ReportsInsightsComponent {

  constructor(private httpClient : HttpClient) {}

  baseUrl = environment.BASE_URL;

  generateStatement(){
    console.log("clicked!");

    this.httpClient.get(`${this.baseUrl}/api/v1/income/account-statement/report/2024-01-01/2026-01-01`, {
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
}
