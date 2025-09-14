import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { DatePipe, NgFor, NgIf } from '@angular/common';
import { environment } from '../../environments/environment';
import { NotificationService } from '../notification-service.service';

interface UserNotification {
  notificationId: number;
  subject: string;
  description: string;
  scheduleFrom: string;
  scheduleTo: string;
  read: boolean;
}

@Component({
  selector: 'app-user-notifications',
  standalone: true,
  imports: [NgIf, NgFor, DatePipe], 
  templateUrl: './user-notifications.component.html',
  styleUrl: './user-notifications.component.css'
})
export class UserNotificationsComponent implements OnInit {

  notifications: UserNotification[] = [];
  selectedIds: number[] = [];
  isLoading = false;

  constructor(private http: HttpClient, private notificationService: NotificationService) {}

  baseUrl = environment.BASE_URL;

  ngOnInit(): void {
    this.loadNotifications();
  }

  loadNotifications() {
    this.isLoading = true;
    this.http.get<UserNotification[]>(`${this.baseUrl}/api/v1/userProfile/get-notifications`)
      .subscribe({
        next: (data) => {
          this.notifications = data;
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Error fetching notifications', err);
          this.isLoading = false;
        }
      });
  }


  toggleSelection(id: number, event: any) {
    if (event.target.checked) {
      this.selectedIds.push(id);
    } else {
      this.selectedIds = this.selectedIds.filter(x => x !== id);
    }
  }

  isSelected(id: number): boolean {
    return this.selectedIds.includes(id);
  }

  markSelectedAsRead() {
    const idsString = this.selectedIds.join(',');
    this.http.put(`${this.baseUrl}/api/v1/userProfile/user-notification/update?ids=${idsString}`, {})
      .subscribe({
        next: () => {
          this.notifications = this.notifications.map(n =>
            this.selectedIds.includes(n.notificationId) ? { ...n, read: true } : n
          );
          this.selectedIds = [];
          // refresh count
          this.notificationService.loadNotificationCount();
        },
        error: (err) => console.error('Error marking notifications as read', err)
      });
  }

}
