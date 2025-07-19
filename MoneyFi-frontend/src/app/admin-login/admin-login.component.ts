import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ChartConfiguration, ChartData } from 'chart.js';
import { AuthApiService } from '../auth-api.service';
import { ToastrService } from 'ngx-toastr';
import { Router, RouterModule } from '@angular/router';
import { LoginCredentials } from '../model/LoginCredentials';
import { CommonModule } from '@angular/common';
import { NgChartsModule } from 'ng2-charts';

@Component({
  selector: 'app-admin-login',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, NgChartsModule,RouterModule],
  templateUrl: './admin-login.component.html',
  styleUrl: './admin-login.component.scss'
})
export class AdminLoginComponent {
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
        username: ['', [Validators.required]],
        password: ['', [Validators.required, Validators.minLength(6)]]
      });
    }
  
    togglePasswordVisibility() {
      this.showPassword = !this.showPassword;
    }
  
    goToSignup() {
      this.router.navigate(['/admin/signup']);
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
            sessionStorage.setItem('moneyfi.auth', response.jwtToken);
            this.toastr.success('Login successful', 'Success', {
              timeOut: 1500  // 
            });
            
            this.router.navigate(['admin/home']);
          },
          error => {
            this.isLoading = false; // Hide loading spinner
            if (error.status === 404) {
              this.toastr.error('User not found. Please sign up.', 'Login Failed');
            } 
            else if (error.status === 401) {
              if (error.error === 'Incorrect password') {
                this.toastr.error('Incorrect password. Please try again.', 'Login Failed');
              } 
              else if (error.error === 'Account Blocked! Please contact admin'){
                this.toastr.error('Account Blocked! Please contact admin');
              }
              else if (error.error === 'Account Deleted! Please contact admin'){
                this.toastr.error('Account Deleted! Please contact admin');
              }
              else {
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
