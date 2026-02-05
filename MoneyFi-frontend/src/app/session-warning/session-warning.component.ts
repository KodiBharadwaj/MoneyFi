import { Component, inject } from '@angular/core';
import { SessionTimerService } from '../services/session-timer.service';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-session-warning',
  standalone: true,
  imports: [],
  templateUrl: './session-warning.component.html',
  styleUrl: './session-warning.component.css'
})
export class SessionWarningComponent {

  show$ = this.sessionService.showWarning$;
  remaining$ = this.sessionService.remainingSeconds$;

  selectedMinutes = 15;

  constructor(
    private sessionService: SessionTimerService,
    private http: HttpClient
  ) {}

  extendSession() {
    this.http.post<any>('/api/auth/extend-session', {
      minutes: this.selectedMinutes
    }).subscribe(res => {
      const newExpiry = res.expiryTime; // timestamp
      localStorage.setItem('token', res.token);
      this.sessionService.reset(newExpiry);
    });
  }

  cancel() {
    // do nothing — will auto logout when timer hits zero
  }
}
