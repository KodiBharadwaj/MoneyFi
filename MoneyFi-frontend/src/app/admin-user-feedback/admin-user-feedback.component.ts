import { Component, OnInit } from '@angular/core';
import { AdminService } from '../services/AdminService';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { ToastrService } from 'ngx-toastr';
import { HttpClient } from '@angular/common/http';
import { ConfirmLogoutDialogComponent } from '../confirm-logout-dialog/confirm-logout-dialog.component';
import { environment } from '../../environments/environment';
import { CommonModule } from '@angular/common';
import { IncomeComponent } from '../income/income.component';
import { ExpensesComponent } from '../expenses/expenses.component';
import { BudgetsComponent } from '../budgets/budgets.component';
import { GoalsComponent } from '../goals/goals.component';
import { OverviewComponent } from '../overview/overview.component';

interface UserFeedbackResponseDto {
  id:number;
  feedbackId: number;
  timeOfFeedback: string;
  rating: number;
  message: string;
}
@Component({
  selector: 'app-admin-user-feedback',
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
  templateUrl: './admin-user-feedback.component.html',
  styleUrl: './admin-user-feedback.component.css'
})
export class AdminUserFeedbackComponent implements OnInit{

  ngOnInit(): void {
    this.fetchFeedbacks();
  } 

  constructor(private adminService: AdminService, private router:Router, private dialog: MatDialog, 
      private route: ActivatedRoute, private httpClient:HttpClient, private toastr: ToastrService) {};

  baseUrl = environment.BASE_URL;
  feedbacks: UserFeedbackResponseDto[] = [];
  loading: boolean = true;
  buttonLoadingId: number | null = null; // track which button is loading


  fetchFeedbacks() {
    this.httpClient.get<UserFeedbackResponseDto[]>(`${this.baseUrl}/api/v1/user-service/admin/user-feedback/get`).subscribe({
      next: (data) => {
        this.feedbacks = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching feedbacks', err);
        this.loading = false;
      }
    });
  }

  getStars(rating: number): number[] {
    return Array(rating).fill(0);
  }

  markAsSeen(feedbackId: number) {
    this.buttonLoadingId = feedbackId; // start loading for this button
    this.httpClient.put(`${this.baseUrl}/api/v1/user-service/admin/user-feedback/update?id=${feedbackId}`, {}).subscribe({
      next: () => {
         this.buttonLoadingId = null; // stop loading
        // Option 1: Refresh the whole list
        this.fetchFeedbacks();

        // Option 2 (lighter): Remove the feedback row locally
        // this.feedbacks = this.feedbacks.filter(f => f.feedbackId !== feedbackId);
      },
      error: (err) => {
        console.error('Error updating feedback', err);
        this.buttonLoadingId = null; // stop loading even on error
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

        this.httpClient.post(`${this.baseUrl}/api/v1/common/logout`, {}, { responseType: 'text' }).subscribe({
          next: (response) => {
            const jsonResponse = JSON.parse(response);
            if(jsonResponse.message === 'Logged out successfully'){
                this.toastr.success(jsonResponse.message, '', {
                timeOut: 1500  // time in milliseconds (3 seconds)
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
