import { HttpClient } from '@angular/common/http';
import { Injectable, OnDestroy } from '@angular/core';
import { interval, Subscription } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class KeepAliveService implements OnDestroy {

  private subscription!: Subscription;

  constructor(private http: HttpClient) {}

  startPinging() {
    this.subscription = interval(60000).subscribe(() => { // every 60s
      console.log("schedule working")
      this.http.get('https://moneyfi-eureka.onrender.com').subscribe();
      this.http.get('https://moneyfi-api-gateway.onrender.com').subscribe();
      this.http.get('https://moneyfi-transaction-service.onrender.com').subscribe();
      this.http.get('https://moneyfi-budget.onrender.com').subscribe();
      this.http.get('https://moneyfi-goal.onrender.com').subscribe();
      this.http.get('https://moneyfi-user.onrender.com').subscribe();
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
