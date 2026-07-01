import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { environment } from '../../../environments/environment';
import { AdminCommonServiceService } from '../admin-common-service.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-admin-triggers',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-triggers.component.html',
  styleUrl: './admin-triggers.component.css',
})
export class AdminTriggersComponent {
  loading = false;
  message = '';

  batches = [
    {
      type: 'INCOME',
      label: 'Income',
      recipientType: 'ALL',
      username: '',
      suggestions: [],
      showSuggestions: false,
    },
    {
      type: 'EXPENSE',
      label: 'Expense',
      recipientType: 'ALL',
      username: '',
      suggestions: [],
      showSuggestions: false,
    },
    {
      type: 'GOAL',
      label: 'Goal',
      recipientType: 'ALL',
      username: '',
      suggestions: [],
      showSuggestions: false,
    },
  ];

  constructor(private adminCommonService: AdminCommonServiceService) {}

  baseUrl = environment.BASE_URL;

  triggerBatch(type: string, recipientType: string, username?: string): void {
    const token = sessionStorage.getItem('moneyfi.auth');

    let url = `${this.baseUrl}/api/v1/transaction/admin/batch-sync?type=${type}`;

    if (recipientType === 'SPECIFIC' && username) {
      url += `&username=${encodeURIComponent(username)}`;
    }

    this.loading = true;

    fetch(url, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
      .then((response) => {
        if (!response.ok) {
          if (response.status === 401) {
            throw new Error('Unauthorized');
          }

          if (response.status === 403) {
            throw new Error('Access denied');
          }

          throw new Error('Batch trigger failed');
        }

        this.message = `${type} batch triggered successfully`;
      })
      .catch((error) => {
        this.message = error.message;
      })
      .finally(() => {
        this.loading = false;
      });
  }

  searchUsers(batch: any) {
    const query = batch.username?.trim();

    if (!query || query.length < 2) {
      batch.suggestions = [];
      batch.showSuggestions = false;
      return;
    }

    this.adminCommonService.getUsernames(0, 10, query).subscribe((data) => {
      batch.suggestions = data;
      batch.showSuggestions = data.length > 0;
    });
  }

  selectUser(batch: any, username: string) {
    batch.username = username;

    batch.showSuggestions = false;
  }

  onFocus(batch: any) {
    if (batch.suggestions.length) {
      batch.showSuggestions = true;
    }
  }

  onBlur(batch: any) {
    setTimeout(() => {
      batch.showSuggestions = false;
    }, 200);
  }
}
