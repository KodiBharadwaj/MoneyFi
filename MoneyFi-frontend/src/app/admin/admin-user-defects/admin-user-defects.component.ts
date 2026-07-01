import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';
import { ConfirmLogoutDialogComponent } from '../../confirm-logout-dialog/confirm-logout-dialog.component';
import { environment } from '../../../environments/environment';
import { CommonModule } from '@angular/common';
import { IncomeComponent } from '../../income/income.component';
import { ExpensesComponent } from '../../expenses/expenses.component';
import { BudgetsComponent } from '../../budgets/budgets.component';
import { GoalsComponent } from '../../goals/goals.component';
import { OverviewComponent } from '../../overview/overview.component';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-admin-user-defects',
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
    FormsModule,
  ],
  templateUrl: './admin-user-defects.component.html',
  styleUrl: './admin-user-defects.component.css',
})
export class AdminUserDefectsComponent implements OnInit {
  filteredDefects: any[] = [];

  selectedReason: string | null = null;
  reasons: string[] = [];
  showReasonDialog = false;
  defectIdForReason: number | null = null;
  customReason: string = '';

  userDefects: any[] = [];

  selectedImage: string | null = null;

  selectedStatus = 'SUBMITTED';

  statuses = [
    { label: 'New', value: 'SUBMITTED' },
    { label: 'Solved', value: 'COMPLETED' },
    { label: 'Pend', value: 'PENDED' },
    { label: 'Ignore', value: 'IGNORED' },
    { label: 'All', value: 'ALL' },
  ];

  offset = 0;
  limit = 10;
  totalCount = 0;

  isGridLoading = false;

  ngOnInit(): void {
    this.loadUserDefects();
  }

  constructor(
    private httpClient: HttpClient,
    private toastr: ToastrService,
  ) {}

  baseUrl = environment.BASE_URL;

  loadUserDefects(): void {
    this.isGridLoading = true;

    const status =
      this.selectedStatus;

    this.httpClient
      .get<any>(`${this.baseUrl}/api/v1/user-service/admin/user-defects/grid`, {
        params: {
          status,
          offset: this.offset,
          limit: this.limit,
        },
      })
      .subscribe({
        next: (response) => {
          this.userDefects = response.data;
          this.totalCount = response.totalCount;

          this.isGridLoading = false;
        },

        error: (err) => {
          this.isGridLoading = false;

          try {
            const errorObj =
              typeof err.error === 'string' ? JSON.parse(err.error) : err.error;

            this.toastr.error(errorObj.message);
          } catch {
            this.toastr.error('Something went wrong');
          }
        },
      });
  }

  viewImage(username: string, defectId: string): void {
    this.httpClient
      .get(
        `${this.baseUrl}/api/v1/user-service/admin/user-defects/image?username=${username}&type=DEFECT&id=${defectId}`,
        { responseType: 'blob' },
      )
      .subscribe({
        next: (blob) => {
          const reader = new FileReader();
          reader.onload = () => {
            this.selectedImage = reader.result as string;
          };
          reader.readAsDataURL(blob);
        },
        error: (err) => {
          if (err.error instanceof Blob) {
            const reader = new FileReader();
            reader.onload = () => {
              try {
                const text = reader.result as string;
                const errorObj = JSON.parse(text);
                this.toastr.error(errorObj.message);
              } catch (e) {
                console.error('Failed to parse Blob error:', e);
                this.toastr.error('Something went wrong');
              }
            };
            reader.readAsText(err.error);
          } else {
            // fallback if it's not a Blob
            const message = err.error?.message || 'Something went wrong';
            this.toastr.error(message);
          }
        },
      });
  }

  filterStatus(status: string) {
    this.selectedStatus = status;

    this.offset = 0;

    this.loadUserDefects();
  }

  nextPage() {
    if (this.offset + this.limit < this.totalCount) {
      this.offset += this.limit;

      this.loadUserDefects();
    }
  }

  previousPage() {
    if (this.offset > 0) {
      this.offset -= this.limit;

      this.loadUserDefects();
    }
  }

  changeLimit() {
    this.offset = 0;

    this.loadUserDefects();
  }

  get endRecord(): number {
    return Math.min(this.offset + this.limit, this.totalCount);
  }

  resetFilterManually() {
    this.selectedStatus = 'All';
    this.filteredDefects = [...this.userDefects];
  }

  updateDefectStatus(defectId: number, status: string) {
    if (status === 'Ignore') {
      this.defectIdForReason = defectId;
      this.showReasonDialog = true; // open popup
      this.fetchIgnoreReasons();
    } else {
      this.httpClient
        .put(
          `${this.baseUrl}/api/v1/user-service/admin/${defectId}/update-defect-status?reason=null`,
          { status },
        )
        .subscribe({
          next: () => {
            this.loadUserDefects();
          },
          error: (err) => {
            console.error('Failed to update defect status:', err);
            try {
              const errorObj =
                typeof err.error === 'string'
                  ? JSON.parse(err.error)
                  : err.error;
              this.toastr.error(errorObj.message);
            } catch (e) {
              console.error('Failed to parse error:', err.error);
            }
          },
        });
    }
  }

  fetchIgnoreReasons(): void {
    this.httpClient
      .get<
        string[]
      >(`${this.baseUrl}/api/v1/user-service/open/reasons-dialog/get?code=8`)
      .subscribe({
        next: (data) => {
          this.reasons = [...data, 'Other'];
        },
        error: (err) => console.error('Failed to load reasons', err),
      });
  }

  submitIgnoreReason(): void {
    if (!this.selectedReason) return;

    let finalReason = this.selectedReason;
    if (this.selectedReason === 'Other' && this.customReason.trim()) {
      finalReason = this.customReason.trim();
    }

    this.httpClient
      .put(
        `${this.baseUrl}/api/v1/user-service/admin/${this.defectIdForReason}/update-defect-status?reason=${encodeURIComponent(finalReason)}`,
        { status: 'Ignore' },
      )
      .subscribe({
        next: () => {
          this.showReasonDialog = false;
          this.selectedReason = null;
          this.customReason = '';
          this.loadUserDefects(); // refresh UI
        },
        error: (err) => console.error('Failed to update with reason', err),
      });
  }
}
