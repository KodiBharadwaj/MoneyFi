import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { JwtToken } from './model/JwtToken';
import { LoginCredentials } from './model/LoginCredentials';
import { SignupCredentials } from './model/SignupCredentials';
import { environment } from '../environments/environment';


@Injectable({
  providedIn: 'root'
})
export class AuthApiService {

  constructor(private authClient:HttpClient) { }

  baseUrl = environment.BASE_URL;
  
  loginApiFunction(loginCredentials:LoginCredentials):Observable<JwtToken>{
    const token = this.authClient.post<JwtToken>(`${this.baseUrl}/api/auth/login`, loginCredentials);
    // console.log(token);
    return token;
  }

  signupApiFunction(signupCredentials:SignupCredentials):Observable<JwtToken>{
    return this.authClient.post<JwtToken>(`${this.baseUrl}/api/auth/register`, signupCredentials);
  }


  
}
