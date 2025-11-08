import { HttpClient } from '@angular/common/http';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule, NgModel } from '@angular/forms';
import { environment } from '../../environments/environment';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-signup-otp-confirm-dialog',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './signup-otp-confirm-dialog.component.html',
  styleUrl: './signup-otp-confirm-dialog.component.css'
})
export class SignupOtpConfirmDialogComponent {

  constructor(private httpClient: HttpClient, private toastr: ToastrService){};

  baseUrl = environment.BASE_URL;

  @Input() email!: string; // ⬅️ receive email from parent
  @Output() otpValidated = new EventEmitter<boolean>();


  otpValue: string = '';

  validateOtp() {
    this.httpClient.get<boolean>(`${this.baseUrl}/api/auth/${this.email}/${this.otpValue}/check-otp/signup`).subscribe({
      next : (response) => {
        if(response){
          this.otpValidated.emit(true); // ✅ Emit boolean
        }
        else {
          this.otpValidated.emit(false); // ✅ Emit boolean
        }
      }, error: (err) => {
      console.error('Error checking OTP:', err);
      try {
          const errorObj = typeof err.error === 'string' ? JSON.parse(err.error) : err.error;
          this.toastr.error(errorObj.message);
        } catch (e) {
          console.error('Failed to parse error:', err.error);
        }
    },
    })
  }

}
