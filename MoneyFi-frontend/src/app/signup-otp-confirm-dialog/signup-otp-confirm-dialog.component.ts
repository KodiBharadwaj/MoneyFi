import { HttpClient } from '@angular/common/http';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule, NgModel } from '@angular/forms';

@Component({
  selector: 'app-signup-otp-confirm-dialog',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './signup-otp-confirm-dialog.component.html',
  styleUrl: './signup-otp-confirm-dialog.component.css'
})
export class SignupOtpConfirmDialogComponent {

  constructor(private httpClient: HttpClient){};

  baseUrl = "http://localhost:8765";

  @Input() email!: string; // ⬅️ receive email from parent
  @Output() otpValidated = new EventEmitter<boolean>();


  otpValue: string = '';

  validateOtp() {
    // Replace this condition with actual OTP logic
    this.httpClient.get<boolean>(`${this.baseUrl}/api/auth/checkOtp/${this.email}/${this.otpValue}`).subscribe({
      next : (response) => {
        if(response){
          this.otpValidated.emit(true); // ✅ Emit boolean
        }
        else {
          this.otpValidated.emit(false); // ✅ Emit boolean
        }
      }
    })
  }

}
