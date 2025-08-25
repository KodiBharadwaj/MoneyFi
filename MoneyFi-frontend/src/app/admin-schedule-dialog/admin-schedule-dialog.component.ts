import { Component, NgModule, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { environment } from '../../environments/environment';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-admin-schedule-dialog',
  templateUrl: './admin-schedule-dialog.component.html',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatDialogModule,
    MatDatepickerModule,
    MatNativeDateModule
  ],
  standalone : true,
  styleUrl: './admin-schedule-dialog.component.css'
})
export class AdminScheduleDialogComponent implements OnInit {
  scheduleForm!: FormGroup;
  allUsers: string[] = [];
  searchResults: string[] = [];
  showSuggestions = false;
  isSubmitting = false;

  selectedUsers: string[] = [];   // multiple users stored here

  baseUrl = environment.BASE_URL;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<AdminScheduleDialogComponent>,
    private http: HttpClient,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.scheduleForm = this.fb.group({
      subject: ['', Validators.required],
      description: ['', Validators.required],
      scheduleFromDate: ['', Validators.required],
      scheduleFromTime: ['', Validators.required],
      scheduleToDate: ['', Validators.required],
      scheduleToTime: ['', Validators.required],
      recipients: ['All', Validators.required]  // All or Specific
    });

    this.http.get<string[]>(`${this.baseUrl}/api/v1/admin/get-usernames`)
      .subscribe(data => this.allUsers = data);
  }

  onRecipientChange(event: Event) {
    const value = (event.target as HTMLSelectElement).value;
    if (value === 'All') {
      this.selectedUsers = []; // clear any previously selected
    }
  }

  searchUsers(event: Event) {
    const input = event.target as HTMLInputElement;
    const query = input.value.trim().toLowerCase();

    if (!query) {
      this.searchResults = [];
      return;
    }

    this.searchResults = this.allUsers
      .filter(user =>
        user.toLowerCase().includes(query) &&
        !this.selectedUsers.includes(user) // exclude already picked
      )
      .slice(0, 10);

    this.showSuggestions = this.searchResults.length > 0;
  }

  addUser(user: string) {
    if (!this.selectedUsers.includes(user)) {
      this.selectedUsers.push(user);
    }
    this.searchResults = [];
    this.showSuggestions = false;
  }

  removeUser(index: number) {
    this.selectedUsers.splice(index, 1);
  }

  onFocus() {
    if (this.searchResults.length > 0) {
      this.showSuggestions = true;
    }
  }

  onBlur() {
    setTimeout(() => this.showSuggestions = false, 200);
  }

  submit() {
    const {
      subject,
      description,
      scheduleFromDate,
      scheduleFromTime,
      scheduleToDate,
      scheduleToTime,
      recipients
    } = this.scheduleForm.value;

    // format datetime helper
    const formatLocalDateTime = (dateStr: string, timeStr: string): string => {
      const [hour, minute] = timeStr.split(':').map(Number);
      const dateObj = new Date(dateStr);
      dateObj.setHours(hour, minute, 0, 0);
      const pad = (n: number) => n.toString().padStart(2, '0');
      return (
        dateObj.getFullYear() + '-' + pad(dateObj.getMonth() + 1) +
        '-' + pad(dateObj.getDate()) + 'T' +
        pad(dateObj.getHours()) + ':' + pad(dateObj.getMinutes()) + ':' +
        pad(dateObj.getSeconds())
      );
    };

    let scheduleFrom: string | null = null;
    if (scheduleFromDate && scheduleFromTime) {
      scheduleFrom = formatLocalDateTime(scheduleFromDate, scheduleFromTime);
    }

    let scheduleTo: string | null = null;
    if (scheduleToDate && scheduleToTime) {
      scheduleTo = formatLocalDateTime(scheduleToDate, scheduleToTime);
    }

    // Build recipients string
    let recipientsValue = 'All';
    if (recipients === 'Specific') {
      recipientsValue = this.selectedUsers.join(',');
    }

    const payload = {
      subject,
      description,
      recipients: recipientsValue,
      scheduleFrom,
      scheduleTo
    };

    this.http.post<string>(`${this.baseUrl}/api/v1/admin/schedule-notification`, payload, { responseType: 'text' as 'json' })
      .subscribe({
        next: (response: string) => {
          this.isSubmitting = false;
          this.dialogRef.close({ success: true, message: response });
        },
        error: (errorResponse) => {
          this.isSubmitting = false;
          console.error('Error scheduling notification:', errorResponse);

          const statusCode = errorResponse.status;
          let errorMessage = 'Error occurred';

          if (typeof errorResponse.error === 'string') {
            try {
              const parsed = JSON.parse(errorResponse.error);
              errorMessage = parsed.message || errorMessage;
            } catch {
              errorMessage = errorResponse.error; // just show raw text
            }
          } else if (errorResponse.error?.message) {
            // Normal JSON case
            errorMessage = errorResponse.error.message;
          }

          if (statusCode === 400) {
            this.toastr.error(errorMessage);
          } else {
            this.toastr.error('Something went wrong! Please try later');
          }
        }
      });
    }

  close() {
    this.dialogRef.close();
  }
}
