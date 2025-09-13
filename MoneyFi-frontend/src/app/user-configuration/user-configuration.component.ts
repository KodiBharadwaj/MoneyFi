import { Component } from '@angular/core';
import { ChangePasswordDialogComponent } from '../change-password-dialog/change-password-dialog.component';
import { HttpClient } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ContactFormComponent } from '../forms/contact-form/contact-form.component';
import { FeedbackFormComponent } from '../forms/feedback-form/feedback-form.component';
import { SurveyFormComponent } from '../forms/survey-form/survey-form.component';

@Component({
  selector: 'app-user-configuration',
  standalone: true,
  imports: [FormsModule, CommonModule, ContactFormComponent, FeedbackFormComponent, SurveyFormComponent],
  templateUrl: './user-configuration.component.html',
  styleUrl: './user-configuration.component.css'
})
export class UserConfigurationComponent {

  constructor(private http: HttpClient, private toastr:ToastrService, private dialog:MatDialog, private router: Router) { }
  baseUrl = environment.BASE_URL;

  selectedFile: File | null = null;
  otp = '';
  description = '';
  blockRequestSent = false;
  loadingBlockRequest = false;
  username = '';
  selectedReason: string = '';
  reasons: string[] = [];

  changePassword() {
    const dialogRef = this.dialog.open(ChangePasswordDialogComponent, {
      width: '400px'
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        // Handle successful password change
        this.toastr.success('Password changed successfully!', '', {
          timeOut:1500
        });
      }
    });
  }

  changeMyName() {
    
    this.http.get(`${this.baseUrl}/api/v1/userProfile/get-username`, { responseType: 'text' }).subscribe({
      next: (data: string) => {
        this.username = data;
        this.callNameChangeRequestMethod(this.username);
      },
      error: (err) => {
        console.error('Error fetching username', err);
      }
    });
  }

  callNameChangeRequestMethod(username : string){
    if(username.length === 0) username = '';
    window.open(`/raise-request?tab=rename&username=${username}`, '_blank');
  }

  trackNameChangeRequest(){
    window.open(`/track-request`, '_blank');
  }

  initiateAccountBlock() {
    this.loadingBlockRequest = true;
    this.http.get(`${this.baseUrl}/api/v1/userProfile/otp-request/block-account`, {
      responseType: 'text'
    }).subscribe({
      next: response => {
        this.blockRequestSent = true;
        this.loadingBlockRequest = false;
        this.toastr.success('Otp sent to your email')
      },
      error: err => {
        alert('Failed to initiate block request.');
        this.loadingBlockRequest = false;
        console.error(err);
      }
    });

    this.reasons = [];
    this.http.get<string[]>(`${this.baseUrl}/api/v1/userProfile/reasons-dialog/get?code=1`).subscribe({
      next: (data) => {
        this.reasons = [...data, 'Other'];
      },
      error: () => {
        this.reasons = ['Other']; // fallback
      }
    });
  }

  downloadProfileTemplate() {
    this.http.get(`${this.baseUrl}/api/v1/userProfile/profile-details-template/download`, {
      responseType: 'blob'
    }).subscribe(blob => {
      const a = document.createElement('a');
      const objectUrl = URL.createObjectURL(blob);
      a.href = objectUrl;
      a.download = 'profile-template.xlsx'; // Filename for downloaded file
      a.click();
      URL.revokeObjectURL(objectUrl);
    }, error => {
      console.error('Download failed:', error);
    });
  }

  onFileSelected(event: Event) {
    const target = event.target as HTMLInputElement;
    this.selectedFile = target.files && target.files.length ? target.files[0] : null;
  }

  uploadUserProfileExcel() {
    if (!this.selectedFile) return;

    const formData = new FormData();
    formData.append('file', this.selectedFile);

    this.http.post(`${this.baseUrl}/api/v1/userProfile/user-profile/excel-upload`, formData, {
      responseType: 'text' // Because backend returns ResponseEntity<String>
    }).subscribe({
      next: response => {
        this.toastr.success('Profile updated! Please check your profile page');
      },
      error: err => {
        alert('Upload failed!');
        console.error(err);
      }
    });
  }

  confirmAccountBlock() {
    const finalReason = this.selectedReason === 'Other' ? this.description : this.selectedReason;
    const payload = {
      otp: this.otp,
      description: finalReason
    };

    this.http.post(`${this.baseUrl}/api/v1/userProfile/block-account`, payload, {
      responseType: 'text'
    }).subscribe({
      next: response => {
        alert('Account block confirmed.');
        this.http.post(`${this.baseUrl}/api/v1/userProfile/logout`, {}, { responseType: 'text' }).subscribe({
          next: (response) => {
            const jsonResponse = JSON.parse(response);
            if(jsonResponse.message === 'Logged out successfully'){
                this.toastr.success(jsonResponse.message, '', {
                timeOut: 1500  // time in milliseconds (3 seconds)
              });
              sessionStorage.removeItem('moneyfi.auth');
              sessionStorage.removeItem('Name');
              this.router.navigate(['']);
            } else if(jsonResponse.message === 'Phone number is empty'){
              alert('Kindly fill Phone number before log out')
            }
            else {
              this.toastr.error('Failed to logout')
            }
          },
          error: (error) => {
            console.error(error);
            this.toastr.error('Failed to logout')
          }
        });
      },
      error: err => {
        alert('Failed to confirm block request.');
        console.error(err);
      }
    });
  }

  selectedForm: string = ''; // Stores the selected form type

  // Available form options
  formOptions = [
    { value: 'contact', label: 'Contact Form' },
    { value: 'feedback', label: 'Feedback Form' }
  ];


  selectForm(event: Event) {
    const target = event.target as HTMLSelectElement; // Explicitly cast to HTMLSelectElement
    this.selectedForm = target.value; // Now TypeScript knows that 'value' exists
  }

}
