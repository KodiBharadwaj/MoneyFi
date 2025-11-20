import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AdminService } from '../services/AdminService';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmLogoutDialogComponent } from '../confirm-logout-dialog/confirm-logout-dialog.component';

@Component({
  selector: 'app-admin-users',
  templateUrl: './admin-users.component.html',
  styleUrls: ['./admin-users.component.css'],
  imports : [CommonModule, FormsModule, RouterModule],
  standalone : true
})
export class AdminUsersComponent implements OnInit {
  status: string = '';
  users: any[] = [];

  nameFilter: string = '';
  usernameFilter: string = '';
  phoneFilter: string = '';
  isAscending: boolean = true;
  isLoading = false;
  isGridLoading = false;


  constructor(private router: ActivatedRoute, private adminService: AdminService, private toastr:ToastrService, private httpClient:HttpClient,
    private route:Router, private dialog: MatDialog
  ) {}

  baseUrl = environment.BASE_URL;

  ngOnInit(): void {
    this.router.paramMap.subscribe(params => {
      this.status = params.get('status') || '';
      this.fetchUsers(this.status);
    });
  }

  fetchUsers(status: string) {
    this.isGridLoading = true;
    this.adminService.getUsersByStatus(status).subscribe({
      next : (data) => {
      this.users = data;
      this.isGridLoading = false;
    },
      error: (err) => {
        console.error('Failed to fetch users', err);
        this.isGridLoading = false;
      }
    });
  }

  // Filter function
  filteredUsers() {
    let filtered = this.users.filter(user =>
      (!this.nameFilter || user.name?.toLowerCase().includes(this.nameFilter.toLowerCase())) &&
      (!this.usernameFilter || user.username?.toLowerCase().includes(this.usernameFilter.toLowerCase())) &&
      (!this.phoneFilter || user.phone?.toString().includes(this.phoneFilter))
    );

    return this.isAscending
      ? filtered
      : [...filtered].reverse();
  }


  // Toggle order
  toggleOrder() {
    this.isAscending = !this.isAscending;
  }

  generateReport() {
    this.isLoading = true;
    this.httpClient.get(`${this.baseUrl}/api/v1/user-service/admin/user-details/excel?status=${this.status}`, { responseType: 'blob' }).subscribe({
      next: (response) => {
        // Trigger File Download
        const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `user_list.xlsx`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
        this.isLoading = false;
      },
      error: (error) => {
        this.isLoading = false;
        console.error('Failed to generate report:', error);
        // alert("Failed to generate the report. Please try again.");
        console.log(error.status)
        console.log(error.error?.message);

        if(error.status === 401){
            if (error.error === 'TokenExpired') {
              alert('Your session has expired. Please login again.');
              sessionStorage.removeItem('moneyfi.auth');
              // this.route.navigate(['/']);
            } else if(error.error === 'Token is blacklisted'){
              alert('Your session has expired. Please login again.');
              sessionStorage.removeItem('moneyfi.auth');
              // this.route.navigate(['/']);
            }
            else if(error.error === 'AuthorizationFailed'){
              alert('Service Unavailable!! Please try later');
            }
          } else if (error.status === 503){
            alert('Service Unavailable!! Please try later');
          } else if (error.status === 404 && error?.message === 'No user data found to generate excel'){
            this.toastr.error('Failed to generate report due to no data');
          }
      }
    });
  }


  logoutUser(): void {
        const dialogRef = this.dialog.open(ConfirmLogoutDialogComponent, {
          width: '400px',
          panelClass: 'custom-dialog-container',
        });
      
        dialogRef.afterClosed().subscribe((result) => {
          if (result) {
    
            this.httpClient.post(`${this.baseUrl}/api/v1/user-admin/logout`, {}, { responseType: 'text' }).subscribe({
              next: (response) => {
                const jsonResponse = JSON.parse(response);
                if(jsonResponse.message === 'Logged out successfully'){
                    this.toastr.success(jsonResponse.message, '', {
                    timeOut: 1500  // time in milliseconds (3 seconds)
                  });
                  sessionStorage.removeItem('moneyfi.auth');
                  this.route.navigate(['admin/login']);
                } 
                else {
                  this.toastr.error('Failed to logout')
                }
              },
              error: (error) => {
                console.error(error);
                this.toastr.error('Failed to logout')
              }
            });
          }
        });
      }

}
