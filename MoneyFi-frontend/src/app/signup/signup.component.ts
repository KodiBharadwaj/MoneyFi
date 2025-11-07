import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { SignupCredentials } from '../model/SignupCredentials';
import { AuthApiService } from '../auth-api.service';
import { HttpClient } from '@angular/common/http';
import { ToastrModule, ToastrService } from 'ngx-toastr';
import { NgChartsModule } from 'ng2-charts';
import { ChartConfiguration, ChartData } from 'chart.js';
import { SignupOtpConfirmDialogComponent } from '../signup-otp-confirm-dialog/signup-otp-confirm-dialog.component';
import { environment } from '../../environments/environment';


@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, NgChartsModule, ToastrModule, SignupOtpConfirmDialogComponent, RouterModule],
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.scss']
})
export class SignupComponent {
  signupForm: FormGroup;
  showPassword = false;
  showConfirmPassword = false;
  currentDate: string = new Date().toISOString().split('T')[0];
  isOtpLoading = false;
  showOtp = false;
  tempSignupCredentials!: SignupCredentials;
  role : string = 'USER';

  public mixedChartData: ChartData<'bar' | 'line'> = {
    labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
    datasets: [
      {
        type: 'bar',
        label: 'Expenses',
        data: [50, 45, 60, 40, 35, 60, 50, 55, 70, 75, 80, 75],
        borderColor: 'rgb(255, 99, 132)',
        backgroundColor: 'rgba(255, 99, 132, 0.2)',
      },
      {
        type: 'line',
        label: 'Savings',
        data: [60, 55, 65, 50, 45, 70, 63, 67, 90, 95, 90, 85],
        fill: false,
        borderColor: 'rgb(54, 162, 235)',
      },
    ],
  };

  public mixedChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    plugins: {
      legend: {
        display: true,
        position: 'top',
      },
      title: {
        display: true,
        text: 'Monthly Finance Overview',
        font: {
          size: 18,
          family: 'Roboto',
        },
        color: '#1e3c72',
      }
    }
  };

  
  constructor(private fb: FormBuilder, private router: Router, private authApiService:AuthApiService,
     private authClient:HttpClient, private toastr: ToastrService) {
    this.signupForm = this.fb.group({
      name: ['', [Validators.required]],
      username: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, { validator: this.passwordMatchValidator });
  }

  passwordMatchValidator(g: FormGroup) {
    return g.get('password')?.value === g.get('confirmPassword')?.value
      ? null : { mismatch: true };
  }

  togglePasswordVisibility(field: 'password' | 'confirmPassword') {
    if (field === 'password') {
      this.showPassword = !this.showPassword;
    } else {
      this.showConfirmPassword = !this.showConfirmPassword;
    }
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }

  baseUrl = environment.BASE_URL;


isLoading: boolean = false; // Controls the loading spinner

onSubmit(signupCredentials: SignupCredentials) {
  this.tempSignupCredentials = signupCredentials;
  this.isOtpLoading = true; // optional: show loading spinner for OTP step

  this.authClient.get(`${this.baseUrl}/api/auth/send-otp/signup?email=${this.tempSignupCredentials.username}&name=${this.tempSignupCredentials.name}`, {
    responseType: 'text'
  }).subscribe({
    next: (response : string) => {

      if (response === 'Email sent successfully!') {
        this.showOtp = true;
        this.toastr.success('Enter otp below')
      } else if (response === 'Cannot send email'){
        this.toastr.error('Error in our internal email service! Please come later!');
      } else {
        // handle case where backend says OTP failed to send
        console.error('OTP not sent');
      }
    },
    error: (err) => {
      console.error('Error sending OTP:', err);
      try {
          const errorObj = typeof err.error === 'string' ? JSON.parse(err.error) : err.error;
          this.toastr.error(errorObj.message);
        } catch (e) {
          console.error('Failed to parse error:', err.error);
        }
    },
    complete: () => {
      this.isOtpLoading = false;
    }
  });
}


onOtpValidated(success: boolean) {
  if (success) {
    this.isLoading = true;
    this.tempSignupCredentials.role = this.role;
    this.authApiService.signupApiFunction(this.tempSignupCredentials)
    .subscribe(
      response => {
        this.isLoading = false;
        this.toastr.success('User registered successfully!', 'Signup success');
        this.router.navigate(['/login']);
      },
      error => {
        console.error('Signup Failed', error);
        this.isLoading = false; // Stop loading

        const msg = error?.error?.message || 'Something went wrong. Please try again.';
        this.toastr.error(msg);
      }
    );
  } else {
    console.log("failed");
  }

  this.showOtp = false;
}

  navigateTo(route: string): void {
    this.router.navigate([route]);
  }

  
  
}