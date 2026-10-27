import { Component, HostListener, NgZone, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IncomeComponent } from '../income/income.component';
import { ExpensesComponent } from '../expenses/expenses.component';
import { BudgetsComponent } from '../budgets/budgets.component';
import { GoalsComponent } from '../goals/goals.component';
import { Router, RouterModule } from '@angular/router';
import { OverviewComponent } from '../overview/overview.component';
import { ConfirmLogoutDialogComponent } from '../confirm-logout-dialog/confirm-logout-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { ProfileComponent } from '../profile/profile.component';
import { AnalysisComponent } from '../analysis/analysis.component';
import { HttpClient } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';
import { environment } from '../../environments/environment';
import { NotificationService } from '../notification-service.service';
import { UserNotification } from '../model/user-notification';

declare const google: any;

interface NavItem {
  route: string;
  label: string;
  icon: string;
  showBadge?: boolean;
}

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    IncomeComponent,
    ExpensesComponent,
    BudgetsComponent,
    GoalsComponent,
    OverviewComponent,
    ConfirmLogoutDialogComponent,
    ProfileComponent,
    RouterModule,
    AnalysisComponent
  ]
})
export class DashboardComponent implements OnInit{

  constructor(private router:Router, private dialog: MatDialog, private ngZone: NgZone, private httpClient:HttpClient, private notificationService: NotificationService,
  private toastr: ToastrService){};

  isLoading = false;
  baseUrl = environment.BASE_URL;
  private eventSource?: EventSource;
  notifications: UserNotification[] = [];
  notificationCount : number = 0;
  isLogoutLoading = false;
  categories: any[] = [];

  gmailSyncEnabled = false;
  isGmailConnected = false;

  private static readonly MOBILE_BREAKPOINT = 768;
  private static readonly SIDEBAR_KEY = 'moneyfi.sidebar.open';

  isMobile = false;
  isSidebarOpen = true;

  navItems: NavItem[] = [
    { route: 'overview', label: 'Overview', icon: 'fas fa-home' },
    { route: 'income', label: 'Income', icon: 'fas fa-money-bill-wave' },
    { route: 'expenses', label: 'Expenses', icon: 'fas fa-receipt' },
    { route: 'budgets', label: 'Budgets', icon: 'fas fa-wallet' },
    { route: 'goals', label: 'Goals', icon: 'fas fa-bullseye' },
    { route: 'analysis', label: 'AI Analysis', icon: 'fas fa-chart-line' },
    { route: 'reports-insights', label: 'Reports/Insights', icon: 'fas fa-lightbulb' },
    { route: 'notifications', label: 'Notifications', icon: 'fa fa-bell', showBadge: true },
  ];

  userItems: NavItem[] = [
    { route: 'profile', label: 'Profile', icon: 'fas fa-user-circle' },
    { route: 'user-configuration', label: 'Settings', icon: 'fas fa-cog' },
  ];

  @HostListener('window:resize')
  onResize(): void {
    const wasMobile = this.isMobile;
    this.isMobile = window.innerWidth <= DashboardComponent.MOBILE_BREAKPOINT;
    if (this.isMobile !== wasMobile) {
      this.isSidebarOpen = !this.isMobile;
    }
  }

  toggleSidebar(): void {
    this.isSidebarOpen = !this.isSidebarOpen;
    if (!this.isMobile) {
      try { localStorage.setItem(DashboardComponent.SIDEBAR_KEY, String(this.isSidebarOpen)); } catch { }
    }
  }

  closeSidebar(): void {
    this.isSidebarOpen = false;
  }

  onNavItemClick(): void {
    if (this.isMobile) this.closeSidebar();
  }

  ngOnInit(): void {
    this.isMobile = window.innerWidth <= DashboardComponent.MOBILE_BREAKPOINT;
    if (this.isMobile) {
      this.isSidebarOpen = false;
    } else {
      try { this.isSidebarOpen = localStorage.getItem(DashboardComponent.SIDEBAR_KEY) !== 'false'; } catch { }
    }

    this.notificationService.notificationCount$.subscribe(count => {
      this.notificationCount = count;
    });
    this.notificationService.loadNotificationCount();
    this.subscribeToNotifications();
  }

  subscribeToNotifications() {
    if (this.eventSource) return;

    const token = sessionStorage.getItem('moneyfi.auth');
    if (!token) return;

    this.eventSource = new EventSource(
      `${this.baseUrl}/api/v1/user-service/sse-notifications/subscribe?token=${encodeURIComponent(token)}`
    );

    this.eventSource.addEventListener('notification', (event: any) => {
      const notification = JSON.parse(event.data);
      this.ngZone.run(() => {
        this.notificationService.addNotification(notification);
      });
    });

    this.eventSource.addEventListener('notification-count', (event: any) => {
      const count = Number(event.data);
      this.ngZone.run(() => {
        this.notificationService.setNotificationCount(count);
      });
    });

    this.eventSource.onerror = (error) => {
      console.error('SSE error', error);
    };
  }

  ngOnDestroy() {
    this.eventSource?.close();
  }


  logoutUser(): void {
    const dialogRef = this.dialog.open(ConfirmLogoutDialogComponent, {
      width: '400px',
      panelClass: 'custom-dialog-container',
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.isLogoutLoading = true;
        this.httpClient.post(`${this.baseUrl}/api/v1/user-service/common/logout`, {}, { responseType: 'text' }).subscribe({
          next: (response) => {
            this.isLogoutLoading = false;
            const jsonResponse = JSON.parse(response);
            if(jsonResponse.message === 'Logged out successfully'){
                this.toastr.success(jsonResponse.message, '', {
                timeOut: 1500  // time in milliseconds (3 seconds)
              });
              sessionStorage.removeItem('moneyfi.auth');
              sessionStorage.clear();
              this.router.navigate(['']);
            } else if(jsonResponse.message === 'Phone number is empty'){
              alert('Kindly fill Phone number before log out')
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

  onGmailSyncClick() {
    this.router.navigate(['dashboard/gmail-sync-summary']);
  }
}

