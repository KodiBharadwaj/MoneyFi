import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { HttpClient } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { ChangePassword } from '../model/ChangePassword';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ToastrService } from 'ngx-toastr';
import { ProfileChangePassword } from '../model/ProfileChangePassword';

@Component({
  selector: 'app-change-password-dialog',
  templateUrl: './change-password-dialog.component.html',
  styleUrls: ['./change-password-dialog.component.scss'],
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    CommonModule
  ]
})
export class ChangePasswordDialogComponent {
  passwordForm: FormGroup;
  hideCurrentPassword = true;
  hideNewPassword = true;
  hideConfirmPassword = true;

  constructor(
    private toastr:ToastrService,
    private httpClient: HttpClient,
    private router: Router,
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<ChangePasswordDialogComponent>,
    private http: HttpClient
  ) {
    this.passwordForm = this.fb.group({
      currentPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, { validator: this.passwordMatchValidator });
  }


  passwordMatchValidator(g: FormGroup) {
    return g.get('newPassword')?.value === g.get('confirmPassword')?.value
      ? null : { mismatch: true };
  }

  baseUrl = "http://localhost:8765";

  isLoading: boolean = false;

  onSubmit() {
    if (this.passwordForm.valid) {
      this.isLoading = true;
    
      const token = sessionStorage.getItem('finance.auth');

      this.httpClient.get<number>(`${this.baseUrl}/auth/token/${token}`).subscribe({
        next: (userId) => {

          const changePasswordDto: ChangePassword = {
            userId: userId,
            currentPassword: this.passwordForm.get('currentPassword')?.value,
            newPassword: this.passwordForm.get('newPassword')?.value
          };
    
          if(changePasswordDto.currentPassword !== changePasswordDto.newPassword){
            this.http.post<ProfileChangePassword>(`${this.baseUrl}/auth/change-password`, changePasswordDto)
            .subscribe({
              next: (profileChangeDto) => {

                if(profileChangeDto.flag === true){
                  this.dialogRef.close(true);
                } 
                else if(profileChangeDto.flag === false && profileChangeDto.otpCount >= 3){
                  alert('Your Password change limit reached for today, Try tomorrow');
                  this.dialogRef.close();
                }
                else {
                  this.toastr.warning('Please enter correct old Password');
                }
                this.isLoading = false;
              },
              error: (error) => {
                console.error('Error changing password:', error);
                this.isLoading = false;
              }
            });

          } else {
            this.toastr.warning('Please enter new password');
            this.isLoading = false;
          }
  
        },
        error: (error) => {
          this.isLoading = false;
          console.error('Failed to fetch userId:', error);
          alert("Session timed out! Please login again");
          sessionStorage.removeItem('finance.auth');
          this.router.navigate(['login']);
        }
      });
      
    }
  }

  onCancel() {
    this.dialogRef.close();
  }
}