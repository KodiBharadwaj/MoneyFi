import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Router } from '@angular/router';
import { NgChartsModule } from 'ng2-charts';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-forgot-username',
  standalone: true,
  imports: [CommonModule,FormsModule,RouterModule,NgChartsModule],
  templateUrl: './forgot-username.component.html',
  styleUrl: './forgot-username.component.css'
})
export class ForgotUsernameComponent {

  formData = {
    phoneNumber: '',
    dateOfBirth: '',
    name: '',
    gender: '',
    maritalStatus: '',
    pincode: ''
  };

  constructor(
    private http: HttpClient,
    private router: Router,
    private toastr: ToastrService
  ) {}

  baseUrl = 'http://localhost:8765'; 

  navigateTo(route: string): void {
    this.router.navigate([route]);
  }

  isLoading: boolean = false;

  submitForm(): void {
    this.isLoading = true;

    this.http.post(`${this.baseUrl}/api/auth/forgotUsername`, this.formData).subscribe({
      next: (response: any) => {
        if(response === true){
          this.isLoading = false;
          this.toastr.success('Username recovery successful. Check your email.', '', {
          timeOut:1500
        });
        this.router.navigate(['/login']);
        } else {
          this.isLoading = false;
          this.toastr.error('Account not found! Try giving correct details');
        }
      },
      error: (error) => {
        console.error(error);
        this.toastr.error('Could not find username with provided details.');
        this.isLoading = false;
      }
    });
  }
}
