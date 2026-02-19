import { HttpClient } from '@angular/common/http';
import { Injectable, OnDestroy } from '@angular/core';
import { interval, Subscription } from 'rxjs';
import { SessionTimerService } from './services/session-timer.service';

@Injectable({
  providedIn: 'root'
})
export class KeepAliveService implements OnDestroy {

  private subscription!: Subscription;

  constructor(private http: HttpClient, private sessionTimerService: SessionTimerService) {}

  startPinging() {
    this.subscription = interval(60000).subscribe(() => { // every 60s
      console.log("schedule working")
      this.http.get('https://moneyfi-eureka.onrender.com').subscribe();
      this.http.get('https://moneyfi-api-gateway.onrender.com').subscribe();
      this.http.get('https://moneyfi-transaction-service.onrender.com').subscribe();
      this.http.get('https://moneyfi-wealth-core.onrender.com').subscribe();
      this.http.get('https://moneyfi-user.onrender.com').subscribe();

      const token = sessionStorage.getItem('moneyfi.auth');
      if (!token) {
        this.stopPinging();
        return;
      }
      const payload = JSON.parse(atob(token.split('.')[1]));
      const expiry = payload.exp * 1000 - (2 * 60 * 1000);
      this.sessionTimerService.start(expiry);
    });
  }

  stopPinging() {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  ngOnDestroy() {
    this.stopPinging();
  }
}
