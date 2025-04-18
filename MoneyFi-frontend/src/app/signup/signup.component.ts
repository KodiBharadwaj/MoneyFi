import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { SignupCredentials } from '../model/SignupCredentials';
import { AuthApiService } from '../auth-api.service';
import { HttpClient } from '@angular/common/http';

import { ToastrModule, ToastrService } from 'ngx-toastr';
import { NgChartsModule } from 'ng2-charts';
import { ChartConfiguration, ChartData } from 'chart.js';
import { SignupOtpConfirmDialogComponent } from '../signup-otp-confirm-dialog/signup-otp-confirm-dialog.component';
import { UserProfile } from '../model/UserProfile';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, NgChartsModule, ToastrModule, SignupOtpConfirmDialogComponent],
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

  baseUrl = "http://localhost:8765";


isLoading: boolean = false; // Controls the loading spinner

onSubmit(signupCredentials: SignupCredentials) {
  this.tempSignupCredentials = signupCredentials;
  this.isOtpLoading = true; // optional: show loading spinner for OTP step

  this.authClient.get(`${this.baseUrl}/api/auth/sendOtpForSignup/${this.tempSignupCredentials.username}/${this.tempSignupCredentials.name}`, {
    responseType: 'text'
  }).subscribe({
    next: (response : string) => {

      if (response === 'Email sent successfully!') {
        this.showOtp = true;
        this.toastr.success('Enter otp below')
      } else if (response === 'User already exists!') {
        alert('User already exists! Please Log in to your account');
        this.router.navigate(['login']);
      } else if (response === 'Cant send email!'){
        this.toastr.error('Error in our internal email service! Please come later!');
      }
       else {
        // handle case where backend says OTP failed to send
        console.error('OTP not sent');
      }
    },
    error: (err) => {
      console.error('Error sending OTP:', err);
      // optionally show error to user
    },
    complete: () => {
      this.isOtpLoading = false;
    }
  });
}


onOtpValidated(success: boolean) {
  if (success) {
    this.isLoading = true;
    this.authApiService.signupApiFunction(this.tempSignupCredentials)
    .subscribe(
      response => {
        sessionStorage.setItem('finance.auth', response.jwtToken);

        // Get the userId from the API
        this.authClient.get<number>(`${this.baseUrl}/api/auth/getUserId/${this.tempSignupCredentials.username}`)
          .subscribe(
            userId => {
              // console.log('User ID:', userId);

              // Use userId in the next API call
              const userprofile = {
                userId : userId,
                name : this.tempSignupCredentials.name,
                email : this.tempSignupCredentials.username,
                createdDate : this.currentDate
              }
              this.authClient.post<UserProfile>(`${this.baseUrl}/api/user/setDetails`, userprofile)
                .subscribe(
                  userProfile => {
                    // console.log('Profile details:', userProfile);
                    this.toastr.success('User registered successfully!', 'Signup success');
                    this.router.navigate(['/login']);
                  },
                  error => {
                    console.error('Failed to save profile details', error);
                  }
                ).add(() => {
                  this.isLoading = false; // Stop loading after this request
                });

              sessionStorage.removeItem('finance.auth');
            },
            error => {
              console.error('Failed to fetch User ID', error);
              this.isLoading = false; // Stop loading
            }
          );
      },
      error => {
        console.error('Signup Failed', error);
        this.isLoading = false; // Stop loading

        // Check if the error response indicates user already exists
        if (error.status === 409) {
          this.toastr.error('User already exists!', 'Signup Error');
        }
      }
    );
  } else {
    // handle OTP failure (optional)
    console.log("failed");
  }

  this.showOtp = false;
}

// onSubmit(signupCredentials: SignupCredentials) {
//   this.isLoading = true; // Start loading


//   this.authApiService.signupApiFunction(signupCredentials)
//     .subscribe(
//       response => {
//         sessionStorage.setItem('finance.auth', response.jwtToken);

//         // Get the userId from the API
//         this.authClient.get<number>(`${this.baseUrl}/api/auth/getUserId/${signupCredentials.username}`)
//           .subscribe(
//             userId => {
//               // console.log('User ID:', userId);

//               // Use userId in the next API call
//               const userprofile = {
//                 userId : userId,
//                 name : signupCredentials.name,
//                 email : signupCredentials.username,
//                 createdDate : this.currentDate
//               }
//               this.authClient.post<UserProfile>(`${this.baseUrl}/api/user/setDetails`, userprofile)
//                 .subscribe(
//                   userProfile => {
//                     // console.log('Profile details:', userProfile);
//                     this.toastr.success('User registered successfully!', 'Signup success');
//                     this.router.navigate(['/login']);
//                   },
//                   error => {
//                     console.error('Failed to save profile details', error);
//                   }
//                 ).add(() => {
//                   this.isLoading = false; // Stop loading after this request
//                 });

//               sessionStorage.removeItem('finance.auth');
//             },
//             error => {
//               console.error('Failed to fetch User ID', error);
//               this.isLoading = false; // Stop loading
//             }
//           );
//       },
//       error => {
//         console.error('Signup Failed', error);
//         this.isLoading = false; // Stop loading

//         // Check if the error response indicates user already exists
//         if (error.status === 409) {
//           this.toastr.error('User already exists!', 'Signup Error');
//         }
//       }
//     );
// }



  navigateTo(route: string): void {
    this.router.navigate([route]);
  }

  
  
}