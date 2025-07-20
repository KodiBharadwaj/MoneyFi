// admin.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private baseUrl = environment.BASE_URL;

  constructor(private http: HttpClient) {}

  getUserCounts(): Observable<any> {
    return this.http.get(`${this.baseUrl}/api/v1/admin/overview-user-details`);
  }

  getUsersByStatus(status: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/api/v1/admin/user-details/grid?status=${status}`);
  }

  getUserRequestsByStatus(status: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/api/v1/admin/fetch-user-requests/grid?status=${status}`);
  }
}
