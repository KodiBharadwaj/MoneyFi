import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, NgForm, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';
import { NgChartsModule } from 'ng2-charts';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { environment } from '../../environments/environment';

export interface RequestStatus {
  email: string;
  name: string;
  requestType: string;
  isRequestActive: string;
  requestStatus: string;
  requestedDate: string;
  description?: string;
}

export interface ApiError {
  message: string;
  code?: string;
}

@Component({
  selector: 'app-request-tracker',
  templateUrl: './request-tracker.component.html',
  styleUrls: ['./request-tracker.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NgChartsModule, ReactiveFormsModule]
})
export class RequestTrackerComponent implements OnInit {
  trackingForm: FormGroup;
  isLoading = false;
  requestStatus: RequestStatus | null = null;
  errorMessage: string | null = null;
  submittedRefNumber: string = '';

  private readonly API_BASE_URL = environment.BASE_URL;

  constructor(
    private formBuilder: FormBuilder,
    private http: HttpClient,
    private router: Router
  ) {
    this.trackingForm = this.formBuilder.group({
      referenceNumber: ['', [
        Validators.required, Validators.minLength(11)
      ]]
    });
  }

  ngOnInit(): void {
    this.setupFormValidation();
  }

  private setupFormValidation(): void {
    this.trackingForm.get('referenceNumber')?.valueChanges.subscribe(() => {
      this.clearErrorMessage();
    });
  }

  onSubmit(): void {
    if (this.trackingForm.valid) {
      const referenceNumber = this.trackingForm.get('referenceNumber')?.value.trim();
      if (referenceNumber) {
        this.trackRequest(referenceNumber);
      }
    } else {
      this.markFormGroupTouched();
    }
  }

  private trackRequest(referenceNumber: string): void {
    this.isLoading = true;
    this.clearErrorMessage();
    this.requestStatus = null;
    this.submittedRefNumber = referenceNumber;

    this.fetchRequestStatus(referenceNumber)
      .pipe(
        catchError((error: HttpErrorResponse) => this.handleError(error)),
        finalize(() => this.isLoading = false)
      )
      .subscribe({
        next: (response: RequestStatus) => {
          this.requestStatus = response;
          this.trackingForm.reset();
        },
        error: (errorMessage: string) => {
          this.errorMessage = errorMessage;
        }
      });
  }

  private fetchRequestStatus(referenceNumber: string): Observable<RequestStatus> {
    return this.http.get<RequestStatus>(`${this.API_BASE_URL}/api/v1/user-service/open/track-user-request?ref=${referenceNumber}`);
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'An unexpected error occurred';

    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = `Error: ${error.error.message}`;
    } else {
      // Server-side error
      switch (error.status) {
        case 400:
          errorMessage = 'Invalid request. Please check your reference number.';
          break;
        case 404:
          errorMessage = 'Request not found. Please verify your reference number.';
          break;
        case 500:
          errorMessage = 'Server error. Please try again later.';
          break;
        case 503:
          errorMessage = 'Service temporarily unavailable. Please try again later.';
          break;
        default:
          errorMessage = `Error ${error.status}: ${error.statusText}`;
      }
    }

    return throwError(() => errorMessage);
  }

  private clearErrorMessage(): void {
    this.errorMessage = null;
  }

  private markFormGroupTouched(): void {
    Object.keys(this.trackingForm.controls).forEach(key => {
      this.trackingForm.get(key)?.markAsTouched();
    });
  }


  navigateTo(route: string): void {
    this.router.navigate([route]);
  }
}