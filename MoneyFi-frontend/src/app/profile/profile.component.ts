import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule, MatOption } from '@angular/material/core';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { ToastrService } from 'ngx-toastr';
import { ChangePasswordDialogComponent } from '../change-password-dialog/change-password-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { UserProfile } from '../model/UserProfile';
import { Router } from '@angular/router';

interface UserProfileDetails {
  name: string;
  email: string;
  phone: string;
  gender: string;
  dateOfBirth: string;
  maritalStatus : string;
  address: string;
  incomeRange:number;
  profileImage: string;
  createdDate: string;
}

interface ProfileDetails {
  createdDate: Date | null;
}

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss'],
  standalone: true,
  imports: [FormsModule,
    CommonModule,
    MatFormFieldModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatInputModule,
    MatOption,
    MatIconModule],
})
export class ProfileComponent implements OnInit {
  userProfileDetails: UserProfileDetails = {
    name: '',
    email: '',
    phone: '',
    gender: '',
    dateOfBirth: '',
    maritalStatus: '',
    address: '',
    incomeRange:0,
    profileImage: '',
    createdDate : '',
  };
  
  today : Date = new Date();
  isEditing = false;

  constructor(private http: HttpClient, private toastr:ToastrService, private dialog:MatDialog, private router: Router) { }

  baseUrl = "http://localhost:8765";
  ngOnInit(): void {
    this.getProfile();
  }

  isImageLoading: boolean = true;
  onImageLoad() {
    this.isImageLoading = false;
  }

  getProfile(): void {
    this.http.get<UserProfileDetails>(`${this.baseUrl}/api/v1/userProfile/getProfile`).subscribe({
      next: (data) => {
        this.userProfileDetails = data;
        this.isImageLoading = false;
      },
      error: (error) => {
        this.isImageLoading = false;
        if (error.status === 401) {
          alert('Session expired! Please log in again.');
          sessionStorage.removeItem('moneyfi.auth');
          this.router.navigate(['login']);
        } else {
          alert('Failed to load profile. Please try again later.');
          console.error('Error fetching profile:', error);
        }
      }
    });
  }

  // Save the profile to the backend
  saveProfile(): void {
    this.http.post<UserProfileDetails>(`${this.baseUrl}/api/v1/userProfile/saveProfile`, this.userProfileDetails).subscribe(
      (data) => {
        this.userProfileDetails = data;
        this.isEditing = false;
        this.toastr.success('Profle updated successfully!', '', {
          timeOut:1500
        });
      },
      (error) => {
        console.error('Error saving profile:', error);
        if(error.status === 401){
          alert('Service Unavailable!! Please try later')
        }
      }
    );

  }

  toggleEdit(): void {
    this.isEditing = !this.isEditing;
  }

  onSaveProfile(): void {
    this.saveProfile();
  }

  onImageUpload(event: any): void {
    const file = event.target.files[0];
    const maxSize = 5 * 1024 * 1024; // 5MB
    const allowedTypes = ['image/jpeg', 'image/png', 'image/gif'];

    if (file && allowedTypes.includes(file.type)) {
      if (file.size <= maxSize) {
        const reader = new FileReader();
        reader.onload = (e: any) => {
          this.userProfileDetails.profileImage = e.target.result;
        };
        reader.readAsDataURL(file);
      } else {
        alert('File is too large. Maximum size is 5MB.');
      }
    } else {
      alert('Please select a valid image file (JPEG, PNG, or GIF).');
    }
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
}
