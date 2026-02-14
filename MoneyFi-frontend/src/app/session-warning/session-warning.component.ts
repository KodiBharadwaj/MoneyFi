import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule, HttpClient, HttpHeaders } from '@angular/common/http';
import { SessionTimerService } from '../services/session-timer.service';
import { environment } from '../../environments/environment';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-session-warning',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './session-warning.component.html',
  styleUrl: './session-warning.component.css'
})
export class SessionWarningComponent {

  show$;
  remaining$;

  selectedMinutes = 15;

  constructor(
    private sessionService: SessionTimerService,
    private http: HttpClient,
    private toastr: ToastrService
  ) {
    this.show$ = this.sessionService.showWarning$;
    this.remaining$ = this.sessionService.remainingSeconds$;
  }

  baseUrl = environment.BASE_URL;

  extendSession() {
    const token = sessionStorage.getItem('moneyfi.auth');

  const headers = new HttpHeaders({
    Authorization: `Bearer ${token}`
  });

  this.http.get(`${this.baseUrl}/api/v1/common/extend-session?minutes=${this.selectedMinutes}`,{ headers, responseType: 'text' }).subscribe(
    newToken => {
      sessionStorage.setItem('moneyfi.auth', newToken);
      this.sessionService.hideWarning();
      this.toastr.success('Session extended for ' + this.selectedMinutes + ' minutes');
    });
  } 

  cancel() {
    this.sessionService.hideWarning();
  }  
}
