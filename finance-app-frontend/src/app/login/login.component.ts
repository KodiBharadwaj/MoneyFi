import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthApiService } from '../auth-api.service';
import { LoginCredentials } from '../model/LoginCredentials';
import { ToastrService } from 'ngx-toastr';
import { NgChartsModule } from 'ng2-charts';
import { ChartConfiguration, ChartData } from 'chart.js';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, NgChartsModule,RouterModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  loginForm: FormGroup;
  showPassword = false;
  isLoading = false;


  public radarChartData: ChartData<'radar'> = {
    labels: ['Budgeting', 'Saving', 'Investing', 'Planning', 'Spending', 'Goals'],
    datasets: [
      {
        label: 'Target Status',
        data: [65, 59, 80, 42, 56, 55],
        fill: true,
        backgroundColor: 'rgba(255, 99, 132, 0.2)',
        borderColor: 'rgb(255, 99, 132)',
        pointBackgroundColor: 'rgb(255, 99, 132)',
        pointBorderColor: '#fff',
      },
      {
        label: 'Current Status',
        data: [28, 48, 40, 19, 65, 27],
        fill: true,
        backgroundColor: 'rgba(54, 162, 235, 0.2)',
        borderColor: 'rgb(54, 162, 235)',
        pointBackgroundColor: 'rgb(54, 162, 235)',
        pointBorderColor: '#fff',
      }
    ]
  };

  public radarChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    plugins: {
      legend: {
        position: 'top',
      },
      title: {
        display: true,
        text: 'Financial Health Analysis',
        font: {
          size: 18,
          family: 'Roboto',
        },
        color: '#1e3c72',
      }
    },
    scales: {
      r: {
        ticks: {
          display: false, // Hide the numbers on the scale
        },
        angleLines: {
          display: true, // Keep the radial axis lines
        },
      }
    }
  };

  constructor(private fb: FormBuilder, private router: Router, private authApiService:AuthApiService, private toastr:ToastrService) {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }

  goToSignup() {
    this.router.navigate(['/signup']);
  }

  goToForgotPassword() {
    // Add your forgot password navigation or modal logic here
    console.log('Forgot password clicked');
  }


  onSubmit(loginCredentials: LoginCredentials) {
    this.isLoading = true; // Show loading spinner
  
    this.authApiService.loginApiFunction(loginCredentials)
      .subscribe(
        response => {
          this.isLoading = false; // Hide loading spinner
          sessionStorage.setItem('finance.auth', response.jwtToken);
          this.toastr.success('Login successful', 'Success');
          this.router.navigate(['dashboard']);
        },
        error => {
          this.isLoading = false; // Hide loading spinner
          if (error.status === 404) {
            this.toastr.error('User not found. Please sign up.', 'Login Failed');
          } else if (error.status === 401) {
            if (error.error === 'Incorrect password') {
              this.toastr.error('Incorrect password. Please try again.', 'Login Failed');
            } else {
              this.toastr.error('Invalid username or password', 'Login Failed');
            }
          } else {
            console.error('Login Failed', error);
            this.toastr.error('An error occurred. Please try again.', 'Login Failed');
          }
        }
      );
  }
  
  

  navigateTo(route: string): void {
    this.router.navigate([route]);
  }
  
}