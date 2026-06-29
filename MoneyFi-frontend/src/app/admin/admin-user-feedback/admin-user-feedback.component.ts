import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { ConfirmLogoutDialogComponent } from '../../confirm-logout-dialog/confirm-logout-dialog.component';
import { environment } from '../../../environments/environment';
import { CommonModule } from '@angular/common';
import { IncomeComponent } from '../../income/income.component';
import { ExpensesComponent } from '../../expenses/expenses.component';
import { BudgetsComponent } from '../../budgets/budgets.component';
import { GoalsComponent } from '../../goals/goals.component';
import { OverviewComponent } from '../../overview/overview.component';
import { FormsModule } from '@angular/forms';

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
    FormsModule
  ],
  templateUrl: './admin-user-feedback.component.html',
  styleUrl: './admin-user-feedback.component.css'
})
export class AdminUserFeedbackComponent implements OnInit{

  ngOnInit(): void {
    this.fetchFeedbacks();
  } 

  constructor(private httpClient:HttpClient) {};

  baseUrl = environment.BASE_URL;
  feedbacks: UserFeedbackResponseDto[] = [];
  loading: boolean = true;
  buttonLoadingId: number | null = null; // track which button is loading

  offset = 0;
  limit = 10;
  totalCount = 0;


  fetchFeedbacks() {
    this.httpClient.get<any>(`${this.baseUrl}/api/v1/user-service/admin/user-feedback/get`, {
      params : {
          offset: this.offset,
          limit: this.limit,
      }
    }).subscribe({
      next: (data) => {
        this.feedbacks = data.data;
        this.loading = false;
        this.totalCount = data.totalCount;
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

  nextPage() {
    if (this.offset + this.limit < this.totalCount) {
      this.offset += this.limit;

      this.fetchFeedbacks();
    }
  }

  previousPage() {
    if (this.offset > 0) {
      this.offset -= this.limit;

      this.fetchFeedbacks();
    }
  }

  changeLimit() {
    this.offset = 0;

    this.fetchFeedbacks();
  }

  get endRecord(): number {
    return Math.min(this.offset + this.limit, this.totalCount);
  }
}
