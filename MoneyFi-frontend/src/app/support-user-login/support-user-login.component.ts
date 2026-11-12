import { Component } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { AuthApiService } from '../auth-api.service';
import { ToastrService } from 'ngx-toastr';
import { Router, RouterModule } from '@angular/router';
import { LoginCredentials } from '../model/LoginCredentials';
import { CommonModule } from '@angular/common';

const ROLE_HOME_ROUTES: Record<string, string> = {
  ADMIN: 'admin/home',
  MAINTAINER: 'maintainer/home',
  CUSTOMER_CARE: 'customer-care/home',
};

@Component({
  selector: 'app-support-user-login',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterModule],
  templateUrl: './support-user-login.component.html',
  styleUrl: './support-user-login.component.scss',
})
export class SupportUserLoginComponent {
  loginForm: FormGroup;
  showPassword = false;
  isLoading = false;

  readonly roles = [
    { value: 'ADMIN', label: 'Admin' },
    { value: 'CUSTOMER_CARE', label: 'Customer Care' },
    { value: 'MAINTAINER', label: 'Maintainer' },
  ];

  readonly features = [
    { icon: 'fa-users-cog', title: 'User Management', description: 'Manage users and access permissions' },
    { icon: 'fa-chart-line', title: 'Analytics', description: 'Monitor system insights and reports' },
    { icon: 'fa-file-excel', title: 'Template Control', description: 'Upload and manage excel templates' },
    { icon: 'fa-shield-alt', title: 'Security', description: 'Maintain system integrity and access' },
  ];

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private authApiService: AuthApiService,
    private toastr: ToastrService,
  ) {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      role: ['ADMIN', Validators.required],
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

  hasError(controlName: string, error: string): boolean {
    const control = this.loginForm.get(controlName);
    return !!control?.touched && !!control.errors?.[error];
  }

  onSubmit(loginCredentials: LoginCredentials) {
    this.isLoading = true;

    this.authApiService.loginApiFunction(loginCredentials).subscribe(
      (response) => {
        const role = Object.keys(response)[0];
        const token = response[role];
        this.isLoading = false;

        const homeRoute = ROLE_HOME_ROUTES[role];
        if (!homeRoute) {
          this.toastr.error('User is not authorized to login');
          return;
        }

        sessionStorage.setItem('moneyfi.auth', token);
        this.toastr.success('Login successful', 'Success', { timeOut: 1500 });
        this.router.navigate([homeRoute]);
      },
      (err) => {
        this.isLoading = false;
        try {
          const errorObj =
            typeof err.error === 'string' ? JSON.parse(err.error) : err.error;
          const message =
            errorObj?.message || errorObj?.error || 'Something went wrong';
          this.toastr.error(message, 'Login Failed');
        } catch (e) {
          console.error('Failed to parse error:', err.error);
          this.toastr.error('An error occurred', 'Login Failed');
        }
      },
    );
  }

  navigateTo(route: string): void {
    this.router.navigate([route]);
  }
}
