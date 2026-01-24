import { AfterViewInit, Component, ElementRef } from '@angular/core';
import { ChangePasswordDialogComponent } from '../change-password-dialog/change-password-dialog.component';
import { HttpClient } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
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
export class UserConfigurationComponent implements AfterViewInit{

  constructor(private http: HttpClient, private toastr:ToastrService, private dialog:MatDialog, private router: Router,
    private route: ActivatedRoute, private el: ElementRef
  ) { }
  baseUrl = environment.BASE_URL;

  selectedFile: File | null = null;
  otp = '';
  password = '';
  description = '';
  blockRequestSent = false;
  deleteRequestSent = false;
  loadingAccountDeactivationRequest = false;
  loadingAccountDeleteRequest = false;
  username = '';
  selectedReason: string = '';
  blockReasons: string[] = [];
  deleteReasons: string[] = [];
  isDownloading = false;
  isUploading = false;

  ngAfterViewInit() {
    this.route.fragment.subscribe(fragment => {
      if (fragment) {
        const element = this.el.nativeElement.querySelector('#' + fragment);
        if (element) {
          // Scroll to element
          element.scrollIntoView({ behavior: 'smooth', block: 'center' });
          // Add highlight effect
          element.classList.add('highlight');
          setTimeout(() => element.classList.remove('highlight'), 2000);
        }
      }
    });
  }

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
    
    this.http.get(`${this.baseUrl}/api/v1/user/get-username`, { responseType: 'text' }).subscribe({
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
    this.loadingAccountDeactivationRequest = true;
    this.http.get(`${this.baseUrl}/api/v1/user/otp-request/account-deactivate-actions?type=BLOCK`, {
      responseType: 'text'
    }).subscribe({
      next: response => {
        this.blockRequestSent = true;
        this.loadingAccountDeactivationRequest = false;
        this.toastr.success('Otp sent to your email')
      },
      error: err => {
        this.loadingAccountDeactivationRequest = false;
        try {
          const errorObj = typeof err.error === 'string' ? JSON.parse(err.error) : err.error;
          this.toastr.error(errorObj.message);
        } catch (e) {
          console.error('Failed to parse error:', err.error);
        }
      }
    });

    this.blockReasons = [];
    this.http.get<string[]>(`${this.baseUrl}/api/v1/user-service/open/reasons-dialog/get?code=1`).subscribe({
      next: (data) => {
        this.blockReasons = [...data, 'Other'];
      },
      error: () => {
        this.blockReasons = ['Other']; // fallback
      }
    });
  }


  initiateAccountDelete() {
    this.loadingAccountDeleteRequest = true;
    this.http.get(`${this.baseUrl}/api/v1/user/otp-request/account-deactivate-actions?type=DELETE`, {
      responseType: 'text'
    }).subscribe({
      next: response => {
        this.deleteRequestSent = true;
        this.loadingAccountDeleteRequest = false;
        this.toastr.success('Otp sent to your email')
      },
      error: err => {
        alert('Failed to initiate delete request.');
        this.loadingAccountDeleteRequest = false;
        console.error(err);
      }
    });

    this.deleteReasons = [];
    this.http.get<string[]>(`${this.baseUrl}/api/v1/user-service/open/reasons-dialog/get?code=5`).subscribe({
      next: (data) => {
        this.deleteReasons = [...data, 'Other'];
      },
      error: () => {
        this.deleteReasons = ['Other']; // fallback
      }
    });
  }

  downloadProfileTemplate() {
    this.isDownloading = true;
    this.http.get(`${this.baseUrl}/api/v1/user-service/user/profile-details-template/download`, {
      responseType: 'blob'
    }).subscribe(blob => {
      const a = document.createElement('a');
      const objectUrl = URL.createObjectURL(blob);
      a.href = objectUrl;
      a.download = 'profile-template.xlsx'; // Filename for downloaded file
      a.click();
      URL.revokeObjectURL(objectUrl);
      this.isDownloading = false;
    }, error => {
      console.error('Download failed:', error);
      this.isDownloading = false;
    });
  }

  onFileSelected(event: Event) {
    const target = event.target as HTMLInputElement;
    this.selectedFile = target.files && target.files.length ? target.files[0] : null;
  }

  uploadUserProfileExcel() {
    if (!this.selectedFile) return;

    this.isUploading = true;
    const formData = new FormData();
    formData.append('file', this.selectedFile);

    this.http.post(`${this.baseUrl}/api/v1/user-service/user/user-profile/excel-upload`, formData, {
      responseType: 'text' // Because backend returns ResponseEntity<String>
    }).subscribe({
      next: response => {
        this.toastr.success('Profile updated! Redirecting you to profile page...');
        this.isUploading = false;
        this.router.navigate(['/dashboard/profile']);
      },
      error: err => {
        console.error(err);
        this.isUploading = false;
        try {
          const errorObj = typeof err.error === 'string' ? JSON.parse(err.error) : err.error;
          this.toastr.error(errorObj.message);
        } catch (e) {
          console.error('Failed to parse error:', err.error);
        }
      }
    });
  }

  confirmAccountBlock() {
    const finalReason = this.selectedReason === 'Other' ? this.description : this.selectedReason;
    const payload = {
      otp: this.otp,
      description: finalReason,
      deactivationType : 'BLOCK',
      password : null
    };

    this.http.post(`${this.baseUrl}/api/v1/user-service/user/deactivate-account`, payload, {
      responseType: 'text'
    }).subscribe({
      next: response => {
        alert('Account block confirmed.');
        this.http.post(`${this.baseUrl}/api/v1/user-admin/logout`, {}, { responseType: 'text' }).subscribe({
          next: (response) => {
            const jsonResponse = JSON.parse(response);
            if(jsonResponse.message === 'Logged out successfully'){
                this.toastr.success(jsonResponse.message, '', {
                timeOut: 1500  // time in milliseconds (3 seconds)
              });
              sessionStorage.removeItem('moneyfi.auth');
              sessionStorage.removeItem('moneyfi.user.name');
              sessionStorage.removeItem('moneyfi.user.profile.image');
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


  confirmAccountDelete() {
    const finalReason = this.selectedReason === 'Other' ? this.description : this.selectedReason;
    const payload = {
      otp: this.otp,
      description: finalReason,
      deactivationType : 'DELETE',
      password : this.password
    };

    this.http.post(`${this.baseUrl}/api/v1/user-service/user/deactivate-account`, payload, {
      responseType: 'text'
    }).subscribe({
      next: response => {
        alert('Account has been deleted. Please raise retrieve request before 30 days to use again.');
        this.http.post(`${this.baseUrl}/api/v1/user-admin/logout`, {}, { responseType: 'text' }).subscribe({
          next: (response) => {
            const jsonResponse = JSON.parse(response);
            if(jsonResponse.message === 'Logged out successfully'){
                this.toastr.success(jsonResponse.message, '', {
                timeOut: 1500  // time in milliseconds (3 seconds)
              });
              sessionStorage.removeItem('moneyfi.auth');
              sessionStorage.removeItem('moneyfi.user.name');
              sessionStorage.removeItem('moneyfi.user.profile.image');
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
