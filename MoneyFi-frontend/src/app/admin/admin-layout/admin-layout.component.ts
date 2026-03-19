import { Component } from '@angular/core';
import { AdminService } from '../../services/AdminService';
import { Router, RouterModule } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { HttpClient } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';
import { environment } from '../../../environments/environment';
import { ConfirmLogoutDialogComponent } from '../../confirm-logout-dialog/confirm-logout-dialog.component';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [RouterModule, FormsModule, CommonModule],
  templateUrl: './admin-layout.component.html',
  styleUrl: './admin-layout.component.css'
})
export class AdminLayoutComponent {

  username: string = '';
  isLogoutLoading = false;

  constructor(private adminService: AdminService, private router:Router, private dialog: MatDialog, 
    private httpClient:HttpClient, private toastr: ToastrService) {}

    baseUrl = environment.BASE_URL;

  ngOnInit() {
    this.getUsernameByToken();
  }

  getUsernameByToken() {
    this.httpClient.get(`${this.baseUrl}/api/v1/common/get-username`, { responseType: 'text' }).subscribe({
      next: (data: string) => {
        this.username = data;
      },
      error: (err) => {
        console.error('Error fetching username', err);
      }
    });
  }

  logoutUser() {
    const dialogRef = this.dialog.open(ConfirmLogoutDialogComponent, {
      width: '400px',
      panelClass: 'custom-dialog-container',
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.isLogoutLoading = true;
        this.httpClient.post(`${this.baseUrl}/api/v1/common/logout`, {}, { responseType: 'text' }).subscribe({
          next: (response) => {
            this.isLogoutLoading = false;
            const jsonResponse = JSON.parse(response);
            if(jsonResponse.message === 'Logged out successfully'){
                this.toastr.success(jsonResponse.message, '', {
                timeOut: 1500  // time in milliseconds (3 seconds)
              });
              sessionStorage.removeItem('moneyfi.auth');
              sessionStorage.clear();
              this.router.navigate(['admin/login']);
            } 
            else {
              this.toastr.error('Failed to logout')
            }
          },
          error: (error) => {
            this.isLogoutLoading = false;
            console.error(error);
            this.toastr.error('Failed to logout')
          }
        });
      }
    });
  }
}
