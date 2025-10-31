import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private baseUrl = environment.BASE_URL;

  // BehaviorSubject keeps last known value
  private notificationCountSubject = new BehaviorSubject<number>(0);
  notificationCount$ = this.notificationCountSubject.asObservable();

  constructor(private http: HttpClient) {}

  // Fetch latest count from backend
  loadNotificationCount(): void {
    this.http.get<number>(`${this.baseUrl}/api/v1/user/get-notifications/count`)
      .subscribe(count => this.notificationCountSubject.next(count));
  }

  // Update count manually (e.g., after marking read)
  setNotificationCount(count: number): void {
    this.notificationCountSubject.next(count);
  }
}
