import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IncomeComponent } from '../income/income.component';
import { ExpensesComponent } from '../expenses/expenses.component';
import { BudgetsComponent } from '../budgets/budgets.component';
import { GoalsComponent } from '../goals/goals.component';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { OverviewComponent } from '../overview/overview.component';
import { ConfirmLogoutDialogComponent } from '../confirm-logout-dialog/confirm-logout-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { ProfileComponent } from '../profile/profile.component';
import { AnalysisComponent } from '../analysis/analysis.component';
import { HttpClient } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';
import { environment } from '../../environments/environment';
import { NotificationService } from '../notification-service.service';

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

  constructor(private router:Router, private dialog: MatDialog, 
    private route: ActivatedRoute, private httpClient:HttpClient, private notificationService: NotificationService,
  private toastr: ToastrService){};

  isLoading = false;
  baseUrl = environment.BASE_URL;
  notificationCount : number = 0;
  isLogoutLoading = false;

  ngOnInit(): void {
    this.notificationService.notificationCount$.subscribe(count => {
      this.notificationCount = count;
    });
    this.notificationService.loadNotificationCount();
  }


  logoutUser(): void {
    const dialogRef = this.dialog.open(ConfirmLogoutDialogComponent, {
      width: '400px',
      panelClass: 'custom-dialog-container',
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.isLogoutLoading = true;
        this.httpClient.post(`${this.baseUrl}/api/v1/user/logout`, {}, { responseType: 'text' }).subscribe({
          next: (response) => {
            this.isLogoutLoading = false;
            const jsonResponse = JSON.parse(response);
            if(jsonResponse.message === 'Logged out successfully'){
                this.toastr.success(jsonResponse.message, '', {
                timeOut: 1500  // time in milliseconds (3 seconds)
              });
              sessionStorage.removeItem('moneyfi.auth');
              localStorage.removeItem('moneyfi.user.name');
              localStorage.removeItem('moneyfi.user.profile.image');
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
  
  
  

  
}

