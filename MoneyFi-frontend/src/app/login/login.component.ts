import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthApiService } from '../auth-api.service';
import { LoginCredentials } from '../model/LoginCredentials';
import { ToastrService } from 'ngx-toastr';
import { NgChartsModule } from 'ng2-charts';
import { ChartConfiguration, ChartData } from 'chart.js';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';

declare const google: any;

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, NgChartsModule,RouterModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit{
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

  constructor(private fb: FormBuilder, private router: Router, private authApiService:AuthApiService, private toastr:ToastrService, private http : HttpClient) {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  baseUrl = environment.BASE_URL;

  ngOnInit(): void {
    setTimeout(() => {
      const buttonDiv = document.getElementById("google-btn");
      if (!buttonDiv) {
        console.error("Google button div not found!");
        return;
      }
      // Initialize Code Client
      const client = google.accounts.oauth2.initCodeClient({
        client_id: environment.GOOGLE_CLIENT_ID,
        scope: "openid email profile",
        ux_mode: "popup",
        callback: (response: any) => this.handleGoogleResponse(response)
      });
      // Trigger requestCode on click
      buttonDiv.addEventListener("click", () => {
        client.requestCode();
      });
    }, 0);
  }

  handleGoogleResponse(response: any) {
    this.isLoading = true;
    this.http.post(`${this.baseUrl}/api/v1/Oauth/google/callback`, { code: response.code })
      .subscribe((res: any) => {
        this.isLoading = false;
        const role = Object.keys(res)[0];
        const token = res[role];
        if (role === 'USER') {
          sessionStorage.setItem('moneyfi.auth', token);
          this.toastr.success('Google login successful', 'Success', { timeOut: 1500 });
          this.router.navigate(['dashboard']);
        } else {
          this.toastr.error('User not authorized to login');
        }
      },
        error => {
          this.isLoading = false; // Hide loading spinner
          if (error.status === 404) {
            this.toastr.error('User not found. Please sign up.', 'Login Failed');
          } 
          else if (error.status === 401) {
            this.toastr.error(error.error.error);
          } else {
            console.error('Login Failed', error);
            this.toastr.error('An error occurred. Please try again.', 'Login Failed');
          }
        }
    );
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
          const role = Object.keys(response)[0];
          const token = response[role];
          this.isLoading = false; // Hide loading spinner
          if(role === 'USER'){
            sessionStorage.setItem('moneyfi.auth', token);
            this.toastr.success('Login successful', 'Success', {
              timeOut: 1500  // 
            });
            
            this.router.navigate(['dashboard']);
          } else this.toastr.error('User is not authorized to login')
        },
        error => {
          this.isLoading = false; // Hide loading spinner
          if (error.status === 404) {
            this.toastr.error('User not found. Please sign up.', 'Login Failed');
          } 
          else if (error.status === 401) {
            this.toastr.error(error.error.error);
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