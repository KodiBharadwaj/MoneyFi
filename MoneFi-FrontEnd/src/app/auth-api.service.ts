import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable, switchMap } from 'rxjs';
import { JwtToken } from './model/JwtToken';
import { LoginCredentials } from './model/LoginCredentials';
import { SignupCredentials } from './model/SignupCredentials';


@Injectable({
  providedIn: 'root'
})
export class AuthApiService {

  constructor(private authClient:HttpClient) { }

  baseUrl = "http://localhost:8765/auth"
  
  loginApiFunction(loginCredentials:LoginCredentials):Observable<JwtToken>{
    const token = this.authClient.post<JwtToken>(this.baseUrl+"/login", loginCredentials);
    console.log(token);
    return token;
  }

  signupApiFunction(signupCredentials:SignupCredentials):Observable<JwtToken>{
    return this.authClient.post<JwtToken>(this.baseUrl+"/register", signupCredentials);
  }


  
}
