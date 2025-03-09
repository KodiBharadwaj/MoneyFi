import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { ToastrService } from 'ngx-toastr';

interface UserProfile {
  name: string;
  email: string;
  phone: string;
  address: string;
  incomeRange:number;
  profileImage: string;
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
    MatIconModule],
})
export class ProfileComponent implements OnInit {
  userProfile: UserProfile = {
    name: '',
    email: '',
    phone: '',
    address: '',
    incomeRange:0,
    profileImage: ''
  };
  
  isEditing = false;
  // userId = 1; // Assuming the user ID is 1, you can get it dynamically if needed

  constructor(private http: HttpClient, private toastr:ToastrService) { }

  baseUrl = "http://localhost:8765";
  ngOnInit(): void {
    this.getProfile();
  }

  // Fetch profile data from backend
  getProfile(): void {
    const token = sessionStorage.getItem('finance.auth');
    // console.log(token);

    this.http.get<number>(`${this.baseUrl}/auth/token/${token}`).subscribe({
      next : (userId) => {
        this.http.get<UserProfile>(`${this.baseUrl}/api/user/profile/${userId}`).subscribe(
          (data) => {
            this.userProfile = data;
          },
          (error) => {
            console.error('Error fetching profile:', error);
          }
        );
      }
    })
  }

  // Save the profile to the backend
  saveProfile(): void {
    const token = sessionStorage.getItem('finance.auth');
    // console.log(token);

    this.http.get<number>(`${this.baseUrl}/auth/token/${token}`).subscribe({
      next : (userId) => {
        this.http.post<UserProfile>(`${this.baseUrl}/api/user/profile/${userId}`, this.userProfile).subscribe(
          (data) => {
            this.userProfile = data;
            this.isEditing = false;
            this.toastr.success('Profle updated successfully!');
          },
          (error) => {
            console.error('Error saving profile:', error);
          }
        );
      }
    })

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
          this.userProfile.profileImage = e.target.result;
        };
        reader.readAsDataURL(file);
      } else {
        alert('File is too large. Maximum size is 5MB.');
      }
    } else {
      alert('Please select a valid image file (JPEG, PNG, or GIF).');
    }
  }
}
