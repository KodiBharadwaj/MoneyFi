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
  imports: [ReactiveFormsModule, CommonModule, NgChartsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  loginForm: FormGroup;
  showPassword = false;
  isLoading = false;
  baseUrl = environment.BASE_URL;

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
      legend: { position: 'top' },
      title: {
        display: true,
        text: 'Financial Health Analysis',
        font: { size: 18, family: 'Roboto' },
        color: '#1e3c72',
      }
    },
    scales: {
      r: { ticks: { display: false }, angleLines: { display: true } }
    }
  };

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private authApiService: AuthApiService,
    private toastr: ToastrService,
    private http: HttpClient
  ) {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  ngOnInit(): void {
    this.initGoogleLogin();
    this.initGithubLogin();
    // this.initFacebookLogin();
  }

  /** GOOGLE LOGIN */
  private initGoogleLogin() {
    setTimeout(() => {
      const buttonDiv = document.getElementById("google-btn");
      if (!buttonDiv) return console.error("Google button div not found!");

      const client = google.accounts.oauth2.initCodeClient({
        client_id: environment.GOOGLE_CLIENT_ID,
        scope: "openid email profile",
        ux_mode: "popup",
        callback: (response: any) => this.handleGoogleResponse(response)
      });

      buttonDiv.addEventListener("click", () => client.requestCode());
    }, 0);
  }

  private handleGoogleResponse(response: any) {
    this.isLoading = true;
    this.http.post(`${this.baseUrl}/api/v1/Oauth/google/callback`, { code: response.code })
      .subscribe({
        next: (res: any) => {
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
        error: err => {
          this.isLoading = false;
          if (err.status === 404) this.toastr.error('User not found. Please sign up.', 'Login Failed');
          else if (err.status === 401) this.toastr.error(err.error.error);
          else {
            console.error('Login Failed', err);
            this.toastr.error('An error occurred. Please try again.', 'Login Failed');
          }
        }
      });
  }

  private initGithubLogin() {
  const githubBtn = document.getElementById("github-btn");
  if (!githubBtn) return console.error("GitHub button not found!");

  githubBtn.addEventListener("click", () => {
    const clientId = environment.GITHUB_CLIENT_ID;
    const redirectUri = `${this.baseUrl}/api/v1/Oauth/github/popup-callback`;
    const scope = "read:user user:email";
    const githubAuthUrl = `https://github.com/login/oauth/authorize?client_id=${clientId}&redirect_uri=${redirectUri}&scope=${scope}`;

    const width = 600;
    const height = 700;
    const left = (window.screen.width / 2) - (width / 2);
    const top = (window.screen.height / 2) - (height / 2);

    const popup = window.open(
      githubAuthUrl,
      "GitHub Login",
      `width=${width},height=${height},top=${top},left=${left}`
    );

    window.addEventListener("message", (event) => {
    const allowedOrigin = this.baseUrl;
    if (event.origin !== allowedOrigin) return;

    const res = event.data;
    console.log(res);

    if (res.USER) {
      sessionStorage.setItem('moneyfi.auth', res.USER);
      this.toastr.success('GitHub login successful', 'Success', { timeOut: 1500 });
      this.router.navigate(['dashboard']);
    } else if (res.ERROR) {
      this.toastr.error(res.ERROR, 'Login Failed', { timeOut: 3000 });
    } else {
      this.toastr.error('GitHub login failed', 'Error');
    }
  }, { once: true });

  });
}

private fbClientId = '1488343345815971';
  private redirectUri = 'https://localhost:8765/api/v1/Oauth/facebook/callback';

 initFacebookLogin(){
  const state = Math.random().toString(36).substring(2); // CSRF token
  const fbAuthUrl = `https://www.facebook.com/v16.0/dialog/oauth?client_id=${this.fbClientId}&redirect_uri=${encodeURIComponent(this.redirectUri)}&state=${state}&scope=email,public_profile&response_type=code`;

    // Open Facebook login popup
    const width = 500, height = 600;
    const left = (screen.width - width) / 2;
    const top = (screen.height - height) / 2;
    const popup = window.open(fbAuthUrl, 'fbLogin', `width=${width},height=${height},top=${top},left=${left}`);

    // Listen for message from popup
    window.addEventListener('message', (event) => {
      if (event.origin !== window.location.origin) return; // security check

      const code = event.data?.code;
      if (code) {
        this.authApiService.loginWithFacebook(code).subscribe({
          next: (res) => {
            console.log('Facebook login successful', res);
            this.authApiService.setFbLoginData(res);
            popup?.close();
          },
          error: (err) => {
            console.error('Facebook login failed', err);
          }
        });
      }
    }, { once: true });
  }


  /** FORM METHODS */
  togglePasswordVisibility() { this.showPassword = !this.showPassword; }
  goToSignup() { this.router.navigate(['/signup']); }
  goToForgotPassword() { console.log('Forgot password clicked'); }

  onSubmit(loginCredentials: LoginCredentials) {
    loginCredentials.role = 'USER';
    this.isLoading = true;
    this.authApiService.loginApiFunction(loginCredentials)
      .subscribe({
        next: response => {
          this.isLoading = false;
          const role = Object.keys(response)[0];
          const token = response[role];
          if (role === 'USER') {
            sessionStorage.setItem('moneyfi.auth', token);
            this.toastr.success('Login successful', 'Success', { timeOut: 1500 });
            this.router.navigate(['dashboard']);
          } else this.toastr.error('User is not authorized to login');
        },
        error: error => {
          this.isLoading = false;
          if (error.status === 404) this.toastr.error('User not found. Please sign up.', 'Login Failed');
          else if (error.status === 401) this.toastr.error(error.error.error);
          else {
            console.error('Login Failed', error);
            this.toastr.error('An error occurred. Please try again.', 'Login Failed');
          }
        }
      });
  }

  navigateTo(route: string): void { this.router.navigate([route]); }
}
