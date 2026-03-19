import { Component } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { AdminScheduleDialogComponent } from '../admin-schedule-dialog/admin-schedule-dialog.component';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';
import { environment } from '../../../environments/environment';
import { CommonModule, DatePipe } from '@angular/common';

@Component({
  selector: 'app-admin-configuration',
  standalone: true,
  imports: [RouterModule, DatePipe, CommonModule],
  templateUrl: './admin-configuration.component.html',
  styleUrl: './admin-configuration.component.css'
})
export class AdminConfigurationComponent {

  constructor(private dialog: MatDialog, private httpClient: HttpClient, private toastr: ToastrService) { }
  baseUrl = environment.BASE_URL;

  showExpiredNotifications = false;
  isExpiredLoading = false;
  expiredSchedules: any[] = [];


  openScheduleNotificationDialog() {
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
        `${this.baseUrl}/api/v1/user-service/admin/schedule-notifications/get?status=EXPIRED&mode=MANUAL`
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
}
