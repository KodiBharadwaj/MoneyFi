import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { ProfileDetails } from '../../model/ProfileDetails';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-contact-form',
  standalone:true,
  imports : [CommonModule, FormsModule],
  templateUrl: './contact-form.component.html',
  styleUrls: ['./contact-form.component.css']
})
export class ContactFormComponent {

  constructor(private httpClient:HttpClient, private router:Router, private toastr:ToastrService){};

  contactData = {
    name: '',
    email: '',
    message: '',
    images: '',
  };
  name : string = '';
  email : string = '';
  selectedFile: File | null = null;
  previewUrl: string | ArrayBuffer | null = null;
  baseUrl = environment.BASE_URL;

  ngOnInit(){
    
    this.getNameAndEmailOfUser();
  }

  getNameAndEmailOfUser(){
    this.httpClient.get<ProfileDetails>(`${this.baseUrl}/api/v1/userProfile/getProfile`).subscribe({
      next: (userProfile) => {
        this.contactData.name = userProfile.name;
        this.contactData.email = userProfile.email;
      },
      error: (error) => {
        console.log('Failed to get the user details', error);
      }
    });
  }


  onFileSelected(event: any): void {
    const file = event.target.files[0];
    const maxSize = 5 * 1024 * 1024; // 5MB
    const allowedTypes = ['image/jpeg', 'image/png', 'image/gif'];

    if (file && allowedTypes.includes(file.type)) {
      if (file.size <= maxSize) {
        const reader = new FileReader();
        reader.onload = (e: any) => {
          this.contactData.images = e.target.result;
        };
        reader.readAsDataURL(file);
      } else {
        alert('File is too large. Maximum size is 5MB.');
      }
    } else {
      alert('Please select a valid image file (JPEG, PNG, or GIF).');
    }
  }


  onSubmit() {
    if (!this.contactData.name || !this.contactData.email || !this.contactData.message) {
      alert('Please fill out all required fields.');
      return;
    }


    const contactDto = {
      name : this.contactData.name,
      email : this.contactData.email,
      message : this.contactData.message,
      images : this.contactData.images || ""
    }

    this.httpClient.post(`${this.baseUrl}/api/v1/userProfile/contactUs`, contactDto).subscribe(
      (response) => {
        this.resetForm();
        this.toastr.success('Feedback submitted successfully!', '', {
          timeOut: 1500  // toast visible for 3 seconds
        });
        
        setTimeout(() => {
          window.location.reload();
        }, 1500);
      },
      error => {
        console.error('Error submitting form:', error);
        alert('Failed to submit form. Please try again.');
      }
    );
  }

  // Reset form after submission
  resetForm() {
    this.contactData = { name: this.contactData.name, email: this.contactData.email, message: '', images: '' };
    this.selectedFile = null;
    this.previewUrl = null;
  }
}
