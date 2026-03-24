import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { SessionGuardService } from './session-gurard.service';
import { ToastrService } from 'ngx-toastr';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const sessionGuard = inject(SessionGuardService);
  const toastr = inject(ToastrService);

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
        sessionStorage.clear();
        router.navigate(['/']);
      } else if (error.status === 503) toastr.error('Service Unavailable. Please try later')
        else if (error.status === 403) toastr.error('You are not authorized to access')
      return throwError(() => error);
    })
  );
};
