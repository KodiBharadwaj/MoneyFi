import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { KeepAliveService } from './keep-alive.service';
import { SessionTimerService } from './services/session-timer.service';
import { SessionWarningComponent } from './session-warning/session-warning.component';


@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, SessionWarningComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit{
  title = 'angular-cursor';

  constructor(private keepAlive: KeepAliveService, private sessionTimerService: SessionTimerService) {}

  ngOnInit() {
    const token = sessionStorage.getItem('moneyfi.auth');
    if (!token) return;

    const payload = JSON.parse(atob(token.split('.')[1]));
    const expiry = payload.exp * 1000;

    this.sessionTimerService.start(expiry);
  }
}