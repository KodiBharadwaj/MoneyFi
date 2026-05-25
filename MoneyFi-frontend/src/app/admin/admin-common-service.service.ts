import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AdminCommonServiceService {

  constructor(private http: HttpClient) { }

  baseUrl = environment.BASE_URL;

  getUsernames(): Observable<string[]> {
    return this.http.get<string[]>(
      `${this.baseUrl}/api/v1/user-service/admin/get-usernames`
    );
  }
}
