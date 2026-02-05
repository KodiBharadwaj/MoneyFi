import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SessionTimerService {

  private warningTimeMs = 10 * 60 * 1000;

  private timeoutId?: number;
  private warningId?: number;
  private countdownId?: number;

  showWarning$ = new BehaviorSubject<boolean>(false);
  remainingSeconds$ = new BehaviorSubject<number>(0);

  start(expiryTime: number) {

    this.clearAll();

    const now = Date.now();
    const totalMs = expiryTime - now;

    if (totalMs <= 0) {
      this.forceLogout();
      return;
    }

    const warningAt = Math.max(totalMs - this.warningTimeMs, 0);

    this.warningId = window.setTimeout(() => {
      this.showWarning$.next(true);
      this.startCountdown(Math.min(600, Math.floor(totalMs / 1000)));
    }, warningAt);

    this.timeoutId = window.setTimeout(() => {
      this.forceLogout();
    }, totalMs);
  }

  private startCountdown(startSeconds: number) {

    this.remainingSeconds$.next(startSeconds);

    this.countdownId = window.setInterval(() => {
      const current = this.remainingSeconds$.value - 1;
      this.remainingSeconds$.next(current);

      if (current <= 0) {
        this.clearCountdown();
      }
    }, 1000);
  }

  reset(newExpiry: number) {
    this.start(newExpiry);
    this.showWarning$.next(false);
  }

  private clearCountdown() {
    if (this.countdownId) {
      clearInterval(this.countdownId);
      this.countdownId = undefined;
    }
  }

  private clearAll() {
    if (this.timeoutId) clearTimeout(this.timeoutId);
    if (this.warningId) clearTimeout(this.warningId);
    this.clearCountdown();
  }

  forceLogout() {
    this.clearAll();
    sessionStorage.clear();
    alert('Timer done')
    // location.href = '/login';
  }
}
