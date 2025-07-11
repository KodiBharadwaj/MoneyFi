import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { environment } from '../../environments/environment';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgChartsModule } from 'ng2-charts';
     
@Component({
  selector: 'app-raise-request',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NgChartsModule],
  templateUrl: './raise-request.component.html',
  styleUrl: './raise-request.component.css'
})
export class RaiseRequestComponent {
  
  constructor(
    private http: HttpClient,
    private router: Router,
    private toastr: ToastrService
  ) {}
  
  baseUrl = environment.BASE_URL; 
  
  navigateHeader(route: string): void {
    this.router.navigate([route]);
  }

  selectedTab: 'unblock' | 'retrieve' | 'rename' = 'unblock';
  email = '';
  loading = false;
  referenceSent = false;

  requestData: any = {};

  selectTab(tab: 'unblock' | 'retrieve' | 'rename') {
    this.selectedTab = tab;
    this.referenceSent = false;
    this.email = '';
    this.requestData = {};
  }

  sendReference() {
    if (!this.email) {
      this.toastr.warning("Please enter your email!");
      return;
    }

    this.loading = true;
    const requestTypeMap: { [key: string]: string } = {
      unblock: 'ACCOUNT_UNBLOCK_REQUEST',
      retrieve: 'ACCOUNT_NOT_DELETE_REQUEST',
      rename: 'NAME_CHANGE_REQUEST'
    };
    const backendRequestType = requestTypeMap[this.selectedTab];

    this.http.get<{ [key: string]: string }>(`${this.baseUrl}/api/auth/${backendRequestType}/${this.email}/reference-number-request`).subscribe({
      next: (response) => {
        console.log('Response from backend:', response);
        const [key, value] = Object.entries(response)[0];

        if(key === 'true'){
          this.toastr.success(value);
          this.requestData.email = this.email;
        } else {
          this.toastr.error(value);
        }

        this.loading = false;
        this.referenceSent = key === 'true';
      },
      error: (errorResponse) => {
        const statusCode = errorResponse.status;
        const errorMessage = errorResponse.error?.message || 'Failed to send reference number';

        if(statusCode === 400)
        this.toastr.error(`${errorMessage}`);

        this.loading = false;
        this.referenceSent = false;
      }
    });
}


  submitRequest() {
    const requestTypeMap: { [key: string]: string } = {
      unblock: 'ACCOUNT_UNBLOCK_REQUEST',
      retrieve: 'ACCOUNT_NOT_DELETE_REQUEST',
      rename: 'NAME_CHANGE_REQUEST'
    };

    const backendRequestType = requestTypeMap[this.selectedTab];

    let body: any;

    if (this.selectedTab === 'rename') {
      const { email, oldName, newName, referenceNumber } = this.requestData;
      body = { email, oldName, newName, referenceNumber };
      this.http.post(`${this.baseUrl}/api/auth/name-change-request`, body).subscribe({
        next: () => {
          this.toastr.success('Request submitted successfully');
          this.selectTab(this.selectedTab);
        },
        error: () => alert('Failed to submit request')
      });

    } else {
      const { email, name, description, referenceNumber } = this.requestData;
      body = { username: email, name, description, referenceNumber };
      this.http.post(`${this.baseUrl}/api/request/${backendRequestType}/raise`, body).subscribe({
        next: () => {
          alert('Request submitted successfully');
          this.selectTab(this.selectedTab); 
        },
        error: () => alert('Failed to submit request')
      });
    }
  }


  navigateTo(path: string) {
    // Add navigation logic or routerLink if using Router
  }
    
}
