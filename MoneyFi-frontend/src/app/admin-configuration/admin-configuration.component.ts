import { Component } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { AdminScheduleDialogComponent } from '../admin-schedule-dialog/admin-schedule-dialog.component';
import { AdminService } from '../services/AdminService';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';
import { ConfirmLogoutDialogComponent } from '../confirm-logout-dialog/confirm-logout-dialog.component';
import { environment } from '../../environments/environment';
import { CommonModule, DatePipe } from '@angular/common';

@Component({
  selector: 'app-admin-configuration',
  standalone: true,
  imports: [RouterModule, DatePipe, CommonModule],
  templateUrl: './admin-configuration.component.html',
  styleUrl: './admin-configuration.component.css'
})
export class AdminConfigurationComponent {

  constructor(private adminService: AdminService, private router:Router, private dialog: MatDialog, 
        private route: ActivatedRoute, private httpClient:HttpClient,
      private toastr: ToastrService) {}
    baseUrl = environment.BASE_URL;

    showExpiredNotifications = false;
    isExpiredLoading = false;
    expiredSchedules: any[] = [];


  openScheduleNotificationDialog(){
    const dialogRef = this.dialog.open(AdminScheduleDialogComponent, {
      width: '600px'
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.toastr.success('Notification Scheduled successfully')
      }
    });
  }

  reuseSchedule(schedule: any) {
    const dialogRef = this.dialog.open(AdminScheduleDialogComponent, {
      width: '600px',
      data: {
        reuse: true,
        schedule
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.toastr.success('Notification reused successfully')
      }
    });
  }

  toggleExpiredNotifications() {
    this.showExpiredNotifications = !this.showExpiredNotifications;

    if (this.showExpiredNotifications && this.expiredSchedules.length === 0) {
      this.getExpiredScheduledNotifications();
    }
  }


  getExpiredScheduledNotifications() {
    this.isExpiredLoading = true;

    this.httpClient
      .get<any[]>(
        `${this.baseUrl}/api/v1/user-service/admin/schedule-notifications/get?status=EXPIRED`
      )
      .subscribe({
        next: (data) => {
          this.expiredSchedules = data;
          this.isExpiredLoading = false;
        },
        error: () => {
          this.toastr.error('Failed to load expired notifications');
          this.isExpiredLoading = false;
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
                timeOut: 1500
              });
              sessionStorage.removeItem('moneyfi.auth');
              this.router.navigate(['admin/login']);
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
