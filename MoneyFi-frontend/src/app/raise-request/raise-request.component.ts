import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { environment } from '../../environments/environment';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgChartsModule } from 'ng2-charts';

export interface RequestStatus {
  userEmail: string;
  name: string;
  requestType: string;
  active: string;
  status: string;
}
export interface ApiError {
  message: string;
  code?: string;
}
@Component({
  selector: 'app-raise-request',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NgChartsModule],
  templateUrl: './raise-request.component.html',
  styleUrl: './raise-request.component.css'
})
export class RaiseRequestComponent implements OnInit {
  
  constructor(
    private http: HttpClient,
    private router: Router,
    private toastr: ToastrService
  ) {}

  ngOnInit() {
    this.selectTab('unblock');
  }
  
  baseUrl = environment.BASE_URL;
  
  // Application state
  selectedTab: 'unblock' | 'retrieve' | 'rename' = 'unblock';
  email = '';
  loading = false;
  showRequestForm = false;
  referenceNumberSent = false;
  skippedToForm = false;
  currentStep = 1;
  
  requestData: any = {};

  navigateHeader(route: string): void {
    this.router.navigate([route]);
  }

  selectTab(tab: 'unblock' | 'retrieve' | 'rename') {
    this.selectedTab = tab;
    this.resetForm();
  }

  resetForm() {
    this.email = '';
    this.requestData = {};
    this.loading = false;
    this.showRequestForm = false;
    this.referenceNumberSent = false;
    this.skippedToForm = false;
    this.currentStep = 1;
  }

  updateProgress(step: number) {
    this.currentStep = step;
  }

  skipToRequestForm() {
    this.skippedToForm = true;
    this.showRequestForm = true;
    this.updateProgress(2);
  }

  sendReference() {
    if (!this.email) {
      this.toastr.warning("Please enter your email address!");
      return;
    }

    if (!this.isValidEmail(this.email)) {
      this.toastr.error("Please enter a valid email address!");
      return;
    }

    this.loading = true;
    const requestTypeMap: { [key: string]: string } = {
      unblock: 'ACCOUNT_UNBLOCK_REQUEST',
      retrieve: 'ACCOUNT_NOT_DELETE_REQUEST',
      rename: 'NAME_CHANGE_REQUEST'
    };
    const backendRequestType = requestTypeMap[this.selectedTab];

    this.http.get<{ [key: string]: string }>(
      `${this.baseUrl}/api/auth/${backendRequestType}/${this.email}/reference-number-request`
    ).subscribe({
      next: (response) => {
        console.log('Response from backend:', response);
        const [key, value] = Object.entries(response)[0];

        if (key === 'true') {
          this.toastr.success(value);
          this.requestData.email = this.email;
          this.referenceNumberSent = true;
          this.showRequestForm = true;
          this.updateProgress(2);
        } else {
          this.toastr.error(value);
        }

        this.loading = false;
      },
      error: (errorResponse) => {
        const statusCode = errorResponse.status;
        const errorMessage = errorResponse.error?.message || 'Failed to send reference number';

        if (statusCode === 400) {
          this.toastr.error(`${errorMessage}`);
        } else {
          this.toastr.error('An error occurred while sending reference number');
        }

        this.loading = false;
      }
    });
  }

  submitRequest() {
    if (!this.validateForm()) {
      this.toastr.error('Please fill all required fields');
      return;
    }

    this.updateProgress(3);

    const requestTypeMap: { [key: string]: string } = {
      unblock: 'ACCOUNT_UNBLOCK_REQUEST',
      retrieve: 'ACCOUNT_NOT_DELETE_REQUEST',
      rename: 'NAME_CHANGE_REQUEST'
    };

    const requestReason = requestTypeMap[this.selectedTab];

    if (this.selectedTab === 'rename') {
      // For rename requests - get email from either source
      const email = this.skippedToForm ? this.requestData.email : this.email;
      const { oldName, newName, referenceNumber } = this.requestData;
      const body = { email, oldName, newName, referenceNumber };
      
      this.http.post(`${this.baseUrl}/api/auth/name-change-request`, body).subscribe({
        next: (response) => {
          this.toastr.success('Request submitted successfully!');
          setTimeout(() => {
            this.resetForm();
          }, 2000);
        },
        error: (errorResponse) => {
            this.updateProgress(2);
            const statusCode = errorResponse.status;
            const errorMessage = errorResponse.error?.message || 'Failed to send request';

            if (statusCode === 400) {
              this.toastr.error(`${errorMessage}`);
            } else {
              this.toastr.error('An error occurred while sending request');
            }

            this.loading = false;
          }
        
      });

    } else {
      // For unblock/retrieve requests - get email from either source
      const email = this.skippedToForm ? this.requestData.email : this.email;
      const { name, description, referenceNumber } = this.requestData;
      const body = { username: email, name, description, referenceNumber, requestReason };
      
      this.http.post(`${this.baseUrl}/api/auth/account-retrieve-request`, body).subscribe({
        next: (response) => {
          this.toastr.success('Request submitted successfully!');
          setTimeout(() => {
            this.resetForm();
          }, 2000);
        },
        error: (errorResponse) => {
          this.updateProgress(2);
          const statusCode = errorResponse.status;
          const errorMessage = errorResponse.error?.message || 'Failed to send request';

          if (statusCode === 400) {
            this.toastr.error(`${errorMessage}`);
          } else {
            this.toastr.error('An error occurred while sending request');
          }

          this.loading = false;
        }
      });
    }
  }

  validateForm(): boolean {
    // Check if email is provided when skipped to form
    if (this.skippedToForm) {
      if (!this.requestData.email || !this.isValidEmail(this.requestData.email)) {
        return false;
      }
    }

    // Check form fields based on selected tab
    if (this.selectedTab === 'rename') {
      return !!(this.requestData.oldName && this.requestData.newName && this.requestData.referenceNumber);
    } else {
      return !!(this.requestData.name && this.requestData.description && this.requestData.referenceNumber);
    }
  }

  isValidEmail(email: string): boolean {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  }

  getFormTitle(): string {
    const titles = {
      unblock: 'Account Unblock Request',
      retrieve: 'Account Retrieve Request',
      rename: 'Name Change Request'
    };
    return titles[this.selectedTab];
  }

  getFormIcon(): string {
    const icons = {
      unblock: 'fas fa-unlock',
      retrieve: 'fas fa-redo',
      rename: 'fas fa-edit'
    };
    return icons[this.selectedTab];
  }

  navigateTo(route: string): void {
    this.router.navigate([route]);
  }
}