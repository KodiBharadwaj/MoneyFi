// import { CanActivateFn } from '@angular/router';

// export const authGuard: CanActivateFn = (route, state) => {
//   return true;
// };

import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';

@Injectable({
  providedIn: 'root',
})
export class AuthGuard implements CanActivate {
  constructor(private router: Router) {}

  canActivate(): boolean {
    const token = sessionStorage.getItem('moneyfi.auth');
    if (token) {
      // User is authenticated
      return true;
    } else {
      // User is not authenticated, redirect to home page
      this.router.navigate(['/']);
      return false;
    }
  }
}
