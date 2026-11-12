import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { environment } from '../../environments/environment';
import { remainingTimeCount } from '../model/remainingTimeCount';

type Stage = 'email' | 'verification' | 'reset';

const OTP_SENT_MESSAGE = 'Verification code sent to your email!';
const OTP_LENGTH = 6;
const MIN_PASSWORD_LENGTH = 6;
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './forgot-password.component.html',
  styleUrl: './forgot-password.component.css'
})
export class ForgotPasswordComponent {
  readonly otpLength = OTP_LENGTH;
  private readonly authUrl = `${environment.BASE_URL}/api/v1/user-service/auth`;

  email = '';
  verificationCode = '';
  newPassword = '';
  confirmPassword = '';

  stage: Stage = 'email';
  errorMessage = '';
  successMessage = '';

  isLoading = false;
  isSubmitting = false; // verify / reset request in flight
  loadingResendOtp = false;

  constructor(
    private http: HttpClient,
    private router: Router,
    private toastr: ToastrService
  ) {}

  // Stage 1: request an OTP (unless one was already sent recently)
  sendVerificationCode() {
    this.clearMessages();

    if (!EMAIL_REGEX.test(this.email)) {
      this.errorMessage = 'Please enter a valid email address';
      return;
    }

    this.isLoading = true;

    this.http.get<remainingTimeCount>(`${this.authUrl}/${this.email}/otp-send/check`).subscribe({
      next: (outputDto) => {
        if (outputDto.result) {
          this.requestOtp();
          return;
        }

        const minutes = outputDto.remainingMinutes;
        alert(`Otp already sent! For new otp, Please try again after ${minutes} ${minutes <= 1 ? 'minute' : 'minutes'}`);
        this.isLoading = false;
        this.stage = 'verification';
      },
      error: (err) => {
        console.error(err);
        this.isLoading = false;
        this.toastApiError(err);
      }
    });
  }

  private requestOtp() {
    this.http.get(`${this.authUrl}/forgot-password/get-otp`, {
      params: { email: this.email },
      responseType: 'text'
    }).subscribe({
      next: (response) => {
        this.isLoading = false;
        if (response === OTP_SENT_MESSAGE) {
          this.successMessage = response;
          this.stage = 'verification';
        } else {
          this.toastr.error('Error in our internal email service! Please come later!');
          this.router.navigate(['login']);
        }
      },
      error: (err) => {
        console.error('Error sending OTP:', err);
        this.isLoading = false;
        this.toastApiError(err);
      }
    });
  }

  // Stage 2: verify the OTP
  verifyCode() {
    this.clearMessages();

    if (this.verificationCode.length !== OTP_LENGTH) {
      this.errorMessage = `Verification code must be ${OTP_LENGTH} digits`;
      return;
    }

    this.isSubmitting = true;

    this.http.get(`${this.authUrl}/forgot-password/verify-otp`, {
      params: { email: this.email, code: this.verificationCode },
      responseType: 'text'
    }).subscribe({
      next: (response) => {
        this.isSubmitting = false;
        this.successMessage = response;
        this.stage = 'reset';
      },
      error: (err) => {
        this.isSubmitting = false;
        this.showApiError(err);
      }
    });
  }

  // Stage 3: set the new password
  resetPassword() {
    this.clearMessages();

    if (!this.validatePassword()) {
      return;
    }

    this.isSubmitting = true;

    this.http.put(`${this.authUrl}/forgot-password/update-password`, null, {
      params: { email: this.email, password: this.newPassword },
      responseType: 'text'
    }).subscribe({
      next: (response) => {
        this.isSubmitting = false;
        this.successMessage = response;
        this.resetForm();
        setTimeout(() => this.router.navigate(['/login']), 2000);
      },
      error: (err) => {
        this.isSubmitting = false;
        this.showApiError(err);
      }
    });
  }

  resendOtp(type: string) {
    this.loadingResendOtp = true;
    this.http.get(`${this.authUrl}/otp-resend?username=${this.email}&type=${type}`).subscribe({
      next: () => {
        this.toastr.success('OTP sent to your email');
        this.loadingResendOtp = false;
      },
      error: (err) => {
        this.loadingResendOtp = false;
        this.toastApiError(err);
      }
    });
  }

  navigateTo(route: string): void {
    this.router.navigate([route]);
  }

  private validatePassword(): boolean {
    if (this.newPassword !== this.confirmPassword) {
      this.errorMessage = 'Passwords do not match';
      return false;
    }
    if (this.newPassword.length < MIN_PASSWORD_LENGTH) {
      this.errorMessage = 'Password must be at least 8 characters long';
      return false;
    }
    return true;
  }

  /** Extracts `message` from an error response body (JSON string or object). */
  private extractErrorMessage(err: any): string | null {
    try {
      const body = typeof err.error === 'string' ? JSON.parse(err.error) : err.error;
      return body.message;
    } catch {
      console.error('Failed to parse error:', err.error);
      return null;
    }
  }

  private showApiError(err: any) {
    const message = this.extractErrorMessage(err);
    if (message) {
      this.errorMessage = message;
    }
  }

  private toastApiError(err: any) {
    const message = this.extractErrorMessage(err);
    if (message) {
      this.toastr.error(message);
    }
  }

  private resetForm() {
    this.email = '';
    this.verificationCode = '';
    this.newPassword = '';
    this.confirmPassword = '';
    this.stage = 'email';
  }

  private clearMessages() {
    this.errorMessage = '';
    this.successMessage = '';
  }
}
