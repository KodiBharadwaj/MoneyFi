import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';
import { UserNotification } from './model/user-notification';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private baseUrl = environment.BASE_URL;

  // BehaviorSubject keeps last known value
  private notificationCountSubject = new BehaviorSubject<number>(0);
  private notificationsListSubject = new BehaviorSubject<UserNotification[]>([]);
  notificationCount$ = this.notificationCountSubject.asObservable();
  notificationList$ = this.notificationsListSubject.asObservable();

  constructor(private http: HttpClient) {}

  // Fetch latest count from backend
  loadNotificationCount(): void {
    this.http.get<number>(`${this.baseUrl}/api/v1/user-service/user/notifications/count`)
      .subscribe(count => this.notificationCountSubject.next(count));
  }

  // Update count manually (e.g., after marking read)
  setNotificationCount(count: number): void {
    this.notificationCountSubject.next(count);
  }

  setNotifications(notifications: UserNotification[]) {
    this.notificationsListSubject.next(notifications);
  }

  addNotification(notification: UserNotification) {
    this.notificationsListSubject.next([
      notification,
      ...this.notificationsListSubject.value
    ]);
  }
}
