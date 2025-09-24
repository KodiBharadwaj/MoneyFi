import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { JwtToken } from './model/JwtToken';
import { LoginCredentials } from './model/LoginCredentials';
import { SignupCredentials } from './model/SignupCredentials';
import { environment } from '../environments/environment';


@Injectable({
  providedIn: 'root'
})
export class AuthApiService {

  constructor(private authClient:HttpClient) { }
  private fbLoginSubject = new Subject<any>();
  fbLogin$ = this.fbLoginSubject.asObservable();

  baseUrl = environment.BASE_URL;
  
  loginApiFunction(loginCredentials:LoginCredentials):Observable<{ [key: string]: string }>{
    return this.authClient.post<{ [key: string]: string }>(`${this.baseUrl}/api/auth/login`, loginCredentials);
    // console.log(token);
  }

  signupApiFunction(signupCredentials:SignupCredentials):Observable<JwtToken>{
    return this.authClient.post<JwtToken>(`${this.baseUrl}/api/auth/register`, signupCredentials);
  }

  loginWithFacebook(code: string): Observable<any> {
    return this.authClient.post('https://your-backend.com/auth/facebook/exchange', { code });
  }

  setFbLoginData(data: any) {
    this.fbLoginSubject.next(data);
  }


  
}
