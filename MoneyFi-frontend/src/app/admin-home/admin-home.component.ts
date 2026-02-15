import { Component, OnInit } from '@angular/core';
import { AdminService } from '../services/AdminService';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ConfirmLogoutDialogComponent } from '../confirm-logout-dialog/confirm-logout-dialog.component';
import { IncomeComponent } from '../income/income.component';
import { ExpensesComponent } from '../expenses/expenses.component';
import { BudgetsComponent } from '../budgets/budgets.component';
import { GoalsComponent } from '../goals/goals.component';
import { OverviewComponent } from '../overview/overview.component';
import { ToastrService } from 'ngx-toastr';
import { MatDialog } from '@angular/material/dialog';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { AdminScheduleDialogComponent } from '../admin-schedule-dialog/admin-schedule-dialog.component';

@Component({
  selector: 'app-admin-home',
  standalone: true,
  imports: [
      CommonModule,
      IncomeComponent,
      ExpensesComponent,
      BudgetsComponent,
      GoalsComponent,
      OverviewComponent,
      ConfirmLogoutDialogComponent,
      RouterModule,
    ],
  templateUrl: './admin-home.component.html',
  styleUrl: './admin-home.component.css'
})
export class AdminHomeComponent implements OnInit {
  totalUsers = 0;
  activeUsers = 0;
  blockedUsers = 0;
  deletedUsers = 0;
  accountUnblockRequests = 0;
  nameChangeRequests = 0;
  accountReactivateRequests = 0;
  otherRequests = 0;
  userDefectRaises = 0;
  userFeedbacks = 0;
  isLogoutLoading = false;

  showGrid = false;
  selectedTile = '';
  gridData: any[] = [];
  schedules: any[] = [];

  baseUrl = environment.BASE_URL;

  constructor(private adminService: AdminService, private router:Router, private dialog: MatDialog, 
      private route: ActivatedRoute, private httpClient:HttpClient,
    private toastr: ToastrService) {}

  ngOnInit(): void {
    this.fetchCounts();
    this.getAdminScheduledNotifications();
  }

  getAdminScheduledNotifications() {
    this.httpClient.get<any[]>(`${this.baseUrl}/api/v1/user-service/admin/schedule-notifications/get?status=ACTIVE`).subscribe({
      next: (data) => {
        this.schedules = data;
      },
      error: () => this.toastr.error('Failed to load schedules')
    });
  }

  cancelSchedule(scheduleId: number) {
    this.httpClient.put(`${this.baseUrl}/api/v1/user-service/admin/schedule-notification/cancel?id=${scheduleId}`, {})
      .subscribe({
        next: () => {
          this.toastr.success('Schedule cancelled');
          this.schedules = this.schedules.map(s => 
            s.scheduleId === scheduleId ? { ...s, cancelled: true } : s
          );
        },
        error: () => this.toastr.error('Failed to cancel schedule')
      });
  }

  deleteSchedule(scheduleId: number) {
    this.httpClient.delete(`${this.baseUrl}/api/v1/user-service/admin/schedule-notification/delete?id=${scheduleId}`)
    .subscribe({
      next: () => {
        this.toastr.success('Schedule Deleted');
        this.getAdminScheduledNotifications();
      },
      error: () => this.toastr.error('Failed to delete schedule')
    })
  }

  editSchedule(schedule: any) {
    const dialogRef = this.dialog.open(AdminScheduleDialogComponent, {
      width: '600px',
      data: {
        mode: 'EDIT',
        schedule
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result?.success) {
        this.getAdminScheduledNotifications(); // refresh grid
      }
    });
  }


  fetchCounts() {
    this.adminService.getUserCounts().subscribe(data => {
        this.totalUsers = data.totalUsers;
        this.activeUsers = data.activeUsers;
        this.blockedUsers = data.blockedUsers;
        this.deletedUsers = data.deletedUsers;
        this.accountUnblockRequests = data.accountUnblockRequests;
        this.nameChangeRequests = data.nameChangeRequests;
        this.accountReactivateRequests = data.accountReactivateRequests;
        this.otherRequests = data.otherRequests;
        this.userDefectRaises = data.userDefectRaises;
        this.userFeedbacks = data.userFeedbacks
    });
  }

  fetchGrid(type: string) {
    this.selectedTile = type;
    this.showGrid = true;
    this.adminService.getUsersByStatus(type).subscribe(users => {
      this.gridData = users;
    });
  }

  navigateTo(route: string) {
    console.log('Navigate to:', route);
  }

  logoutUser(): void {
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
