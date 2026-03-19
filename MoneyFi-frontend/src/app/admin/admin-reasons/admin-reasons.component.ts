import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { environment } from '../../../environments/environment';
import { RouterModule } from '@angular/router';

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

  constructor(private httpClient: HttpClient) { }

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
      this.httpClient.get<Reason[]>(`${this.baseUrl}/api/v1/user-service/admin/reasons/get?code=${category.code}`).subscribe({
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
    this.httpClient.post(`${this.baseUrl}/api/v1/user-service/admin/reasons/add`, payload).subscribe(() => {
      this.loadCategories();
      category.newReason = '';
    });
  }

  updateReason(reason: Reason) {
    const payload = { reasonId: reason.reasonId, reason: reason.reason }; // matches ReasonUpdateRequestDto
    this.httpClient.put(`${this.baseUrl}/api/v1/user-service/admin/reasons/update`, payload).subscribe(() => {
      this.loadCategories();
    });
  }

  deleteReason(reasonId: number) {
    this.httpClient.delete(`${this.baseUrl}/api/v1/user-service/admin/reasons/delete?id=${reasonId}`).subscribe(() => {
      this.loadCategories();
    });
  }
}
