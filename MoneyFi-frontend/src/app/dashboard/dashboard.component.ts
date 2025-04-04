import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IncomeComponent } from '../income/income.component';
import { ExpensesComponent } from '../expenses/expenses.component';
import { BudgetsComponent } from '../budgets/budgets.component';
import { GoalsComponent } from '../goals/goals.component';
import { ActivatedRoute, Route, Router, RouterModule } from '@angular/router';
import { OverviewComponent } from '../overview/overview.component';
import { ConfirmLogoutDialogComponent } from '../confirm-logout-dialog/confirm-logout-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { ProfileComponent } from '../profile/profile.component';
import { AnalysisComponent } from '../analysis/analysis.component';
import { HttpClient } from '@angular/common/http';

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
export class DashboardComponent {

  constructor(private router:Router, private dialog: MatDialog, private route: ActivatedRoute, private httpClient:HttpClient){};

  isLoading = false;


  logoutUser(): void {
    const dialogRef = this.dialog.open(ConfirmLogoutDialogComponent, {
      width: '400px',
      panelClass: 'custom-dialog-container',
    });
  
    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        const token = sessionStorage.getItem('finance.auth');
        this.httpClient.post('http://localhost:8765/auth/logout', {}, { responseType: 'text' }).subscribe({
          next: (response) => {
            console.log(response); // Should print "Logged out successfully"
            sessionStorage.removeItem('finance.auth');
            this.router.navigate(['']);
          },
          error: (error) => {
            console.error(error);
          }
        });
      }
    });
  }
  
  
  

  
}

