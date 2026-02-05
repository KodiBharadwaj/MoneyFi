import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SessionTimerService {

  private warningTimeMs = 10 * 60 * 1000; // 10 min
  private timeoutId: any;
  private warningId: any;

  showWarning$ = new BehaviorSubject<boolean>(false);
  remainingSeconds$ = new BehaviorSubject<number>(0);

  start(expiryTime: number) {
    const now = Date.now();
    const totalMs = expiryTime - now;

    const warningAt = totalMs - this.warningTimeMs;

    this.warningId = setTimeout(() => {
      this.startCountdown();
      this.showWarning$.next(true);
    }, warningAt);

    this.timeoutId = setTimeout(() => {
      this.forceLogout();
    }, totalMs);
  }

  private startCountdown() {
    let seconds = 600;

    this.remainingSeconds$.next(seconds);

    const interval = setInterval(() => {
      seconds--;
      this.remainingSeconds$.next(seconds);

      if (seconds <= 0) clearInterval(interval);
    }, 1000);
  }

  reset(newExpiry: number) {
    clearTimeout(this.warningId);
    clearTimeout(this.timeoutId);
    this.showWarning$.next(false);
    this.start(newExpiry);
  }

  forceLogout() {
    localStorage.clear();
    window.location.href = '/login';
  }
}
