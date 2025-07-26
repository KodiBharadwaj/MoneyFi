import { Component, OnInit } from '@angular/core';
import { AdminService } from '../services/AdminService';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { HttpClient } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';
import { ConfirmLogoutDialogComponent } from '../confirm-logout-dialog/confirm-logout-dialog.component';
import { environment } from '../../environments/environment';
import { CommonModule } from '@angular/common';
import { IncomeComponent } from '../income/income.component';
import { ExpensesComponent } from '../expenses/expenses.component';
import { BudgetsComponent } from '../budgets/budgets.component';
import { GoalsComponent } from '../goals/goals.component';
import { OverviewComponent } from '../overview/overview.component';

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
      ],
  templateUrl: './admin-user-defects.component.html',
  styleUrl: './admin-user-defects.component.css'
})
export class AdminUserDefectsComponent implements OnInit{

  userDefects: any[] = [];
  selectedImage: string | null = null;

  ngOnInit(): void {
    this.loadUserDefects();
  }

  constructor(private adminService: AdminService, private router:Router, private dialog: MatDialog, 
      private route: ActivatedRoute, private httpClient:HttpClient, private toastr: ToastrService) {};

  baseUrl = environment.BASE_URL;

  loadUserDefects(): void {
    this.httpClient.get<any[]>(`${this.baseUrl}/api/v1/admin/user-defects/grid?status=Active`)
      .subscribe({
        next: (data) => {
          this.userDefects = data;
        },
        error: (err) => {
          console.error('Error loading user defects:', err);
        }
      });
  }

  viewImage(imageId: string): void {
    this.httpClient.get(`http://your-backend-url/api/admin/defect-image/${imageId}`, { responseType: 'blob' })
      .subscribe({
        next: (blob) => {
          const reader = new FileReader();
          reader.onload = () => {
            this.selectedImage = reader.result as string;
          };
          reader.readAsDataURL(blob);
        },
        error: (err) => {
          console.error('Error fetching image:', err);
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
    
            this.httpClient.post(`${this.baseUrl}/api/v1/admin/logout`, {}, { responseType: 'text' }).subscribe({
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
