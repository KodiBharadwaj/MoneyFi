import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { environment } from '../../environments/environment';
import { ConfirmLogoutDialogComponent } from '../confirm-logout-dialog/confirm-logout-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { ToastrService } from 'ngx-toastr';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';

interface Reason {
  slNo: number;
  reasonId: number;
  reason: string;
  lastUpdated: Date;
}

interface ReasonCategory {
  code: number;
  title: string;
  reasons: Reason[];
  newReason: string;
}

@Component({
  selector: 'app-admin-reasons',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './admin-reasons.component.html',
  styleUrls: ['./admin-reasons.component.css']
})
export class AdminReasonsComponent implements OnInit {
  categories: ReasonCategory[] = [];

  baseUrl = environment.BASE_URL;

  // Define reason codes with headings
  private REASON_CODES = [
    { code: 1, title: 'Block Account Reasons' },
    { code: 2, title: 'Password Change Reasons' },
    { code: 3, title: 'Name Change Reasons' },
    { code: 4, title: 'Unblock Account Reasons' },
    { code: 5, title: 'Delete Account Reasons' },
    { code: 6, title: 'Account Retrieval Reasons' },
    { code: 7, title: 'Phone Number Change Reasons' },
    { code: 8, title: 'Decline User Request Reasons' },
    { code: 9, title: 'Gmail Sync Count Increase Request' }
  ];

  constructor(private http: HttpClient, private dialog: MatDialog, private toastr: ToastrService, private route: ActivatedRoute, private router:Router) {}

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories() {
    // Prebuild categories in fixed order
    this.categories = this.REASON_CODES.map(cat => ({
      code: cat.code,
      title: cat.title,
      reasons: [],
      newReason: ''
    }));

    // Now fetch reasons for each category and update in place
    this.categories.forEach((category, index) => {
      this.http.get<Reason[]>(`${this.baseUrl}/api/v1/user-service/admin/reasons/get?code=${category.code}`).subscribe({
        next: (data) => {
          this.categories[index].reasons = data;
        },
        error: () => {
          this.categories[index].reasons = [];
        }
      });
    });
  }


  addReason(category: ReasonCategory) {
    if (!category.newReason.trim()) return;
    const payload = { reasonCode: category.code, reason: category.newReason }; // matches ReasonDetailsRequestDto
    this.http.post(`${this.baseUrl}/api/v1/user-service/admin/reasons/add`, payload).subscribe(() => {
      this.loadCategories();
      category.newReason = '';
    });
  }

  updateReason(reason: Reason) {
    const payload = { reasonId: reason.reasonId, reason: reason.reason }; // matches ReasonUpdateRequestDto
    this.http.put(`${this.baseUrl}/api/v1/user-service/admin/reasons/update`, payload).subscribe(() => {
      this.loadCategories();
    });
  }

  deleteReason(reasonId: number) {
    this.http.delete(`${this.baseUrl}/api/v1/user-service/admin/reasons/delete?id=${reasonId}`).subscribe(() => {
      this.loadCategories();
    });
  }


  logoutUser(): void {
      const dialogRef = this.dialog.open(ConfirmLogoutDialogComponent, {
        width: '400px',
        panelClass: 'custom-dialog-container',
      });
    
      dialogRef.afterClosed().subscribe((result) => {
        if (result) {
  
          this.http.post(`${this.baseUrl}/api/v1/user-admin/logout`, {}, { responseType: 'text' }).subscribe({
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
