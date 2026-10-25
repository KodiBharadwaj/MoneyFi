import { Component } from '@angular/core';
import { AdminCommonServiceService } from '../admin-common-service.service';
import { environment } from '../../../environments/environment';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-admin-user-session-time',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-user-session-time.component.html',
  styleUrl: './admin-user-session-time.component.css'
})
export class AdminUserSessionTimeComponent {
  loading = false;
    message = '';
  
    private readonly today = new Date().toLocaleDateString('en-CA');
  
    batches = [
      {
        recipientType: 'ALL',
        username: '',
        timeInMinutes: 0,
        suggestions: [] as string[],
        showSuggestions: false,
      }
    ];
  
    constructor(private adminCommonService: AdminCommonServiceService) {}
  
    baseUrl = environment.BASE_URL;
  
    triggerBatch(
      username?: string,
      timeInMinutes?: number
    ): void {
      const token = sessionStorage.getItem('moneyfi.auth');
  
      let url = `${this.baseUrl}/api/v1/user-service/admin/user-session-time?username=${username}&time=${timeInMinutes}`;
  
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
  
            throw new Error('Failed to modify user session time');
          }
  
          this.message = `Session time for user ${username} has been updated to ${timeInMinutes} minutes`;
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
