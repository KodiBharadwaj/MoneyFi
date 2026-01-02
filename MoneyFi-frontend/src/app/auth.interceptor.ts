import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { SessionGuardService } from './session-gurard.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const sessionGuard = inject(SessionGuardService);

  // Skip auth for public APIs
  if (req.url.includes('/register')) {
    return next(req);
  }
  const jwt = sessionStorage.getItem('moneyfi.auth');
  if (jwt) {
    req = req.clone({
      setHeaders: { Authorization: `Bearer ${jwt}` }
    });
  }
  return next(req).pipe(
    catchError(error => {
      if (
        error.status === 401 &&
        !sessionGuard.sessionExpiredHandled &&
        (error.error === 'TokenExpired' || error.error === 'Token is blacklisted')
      ) {
        sessionGuard.sessionExpiredHandled = true;
        alert('Your session has expired. Please login again.');
        sessionStorage.removeItem('moneyfi.auth');
        localStorage.clear();
        router.navigate(['/']);
      }
      return throwError(() => error);
    })
  );
};
