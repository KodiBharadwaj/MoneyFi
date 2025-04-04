import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';

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
  selectedFile: File | null = null;
  previewUrl: string | ArrayBuffer | null = null;
  baseUrl = "http://localhost:8765";


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
  

    const token = sessionStorage.getItem('finance.auth');
  
    this.httpClient.get<number>(`${this.baseUrl}/auth/token/${token}`).subscribe({
      next: (userId) => {

      this.httpClient.post(`${this.baseUrl}/api/contact/${userId}`, contactDto).subscribe(
      (response) => {
        // alert('Form submitted successfully!');
        this.toastr.success('Form submitted successfully!')
        this.resetForm();
      },
      error => {
        console.error('Error submitting form:', error);
        alert('Failed to submit form. Please try again.');
      }
    );
    
      },
      error: (error) => {
        console.error('Failed to fetch userId:', error);
        alert("Session timed out! Please login again");
        sessionStorage.removeItem('finance.auth');
        this.router.navigate(['login']);
      }
    });
  }

  // Reset form after submission
  resetForm() {
    this.contactData = { name: '', email: '', message: '', images: '' };
    this.selectedFile = null;
    this.previewUrl = null;
  }
}
