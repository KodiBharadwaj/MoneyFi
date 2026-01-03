import { Component, NgZone, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { DatePipe, NgFor, NgIf } from '@angular/common';
import { environment } from '../../environments/environment';
import { NotificationService } from '../notification-service.service';
import { UserNotification } from '../model/user-notification';

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
  private eventSource?: EventSource;

  constructor(private http: HttpClient, private notificationService: NotificationService, private ngZone: NgZone) {}

  baseUrl = environment.BASE_URL;
  userServiceBaseUrl = environment.USER_SERVICE_URL;

  ngOnInit(): void {
    this.notificationService.notificationList$.subscribe(notification => {
      this.notifications = notification;
    });
    this.notificationService.loadNotificationCount();
    this.loadNotifications();
    this.subscribeToNotifications();
  }

  loadNotifications() {
    this.isLoading = true;
    this.http.get<UserNotification[]>(`${this.baseUrl}/api/v1/user-service/user/notifications/get`)
      .subscribe({
        next: (data) => {
          this.notificationService.setNotifications(data);
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Error fetching notifications', err);
          this.isLoading = false;
        }
      });
  }

  subscribeToNotifications() {
    if (this.eventSource) return;

    const token = sessionStorage.getItem('moneyfi.auth');
    if (!token) return;

    this.eventSource = new EventSource(
      `${this.userServiceBaseUrl}/api/v1/user-service/user/sse-notifications/subscribe?token=${encodeURIComponent(token)}`
    );

    this.eventSource.addEventListener('notification', (event: any) => {
      const notification = JSON.parse(event.data);
      this.ngZone.run(() => {
        this.notificationService.addNotification(notification);
      });
    });

    this.eventSource.addEventListener('notification-count', (event: any) => {
      const count = Number(event.data);
      this.ngZone.run(() => {
        this.notificationService.setNotificationCount(count);
      });
    });

    this.eventSource.onerror = (error) => {
      console.error('SSE error', error);
    };
  }

  ngOnDestroy() {
    this.eventSource?.close();
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
    this.http.put(`${this.baseUrl}/api/v1/user-service/user/notification/update?ids=${idsString}`, {})
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
