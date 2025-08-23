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
  standalone : true
})
export class AdminScheduleDialogComponent implements OnInit {
  scheduleForm!: FormGroup;
  userOptions: string[] = []; // usernames fetched from API
  filteredUsers: string[] = [];
  
  baseUrl = environment.BASE_URL;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<AdminScheduleDialogComponent>,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.scheduleForm = this.fb.group({
      subject: ['', Validators.required],
      description: ['', Validators.required],
      scheduleFromDate: ['', Validators.required],
      scheduleFromTime: ['', Validators.required],
      scheduleToDate: ['', Validators.required],
      scheduleToTime: ['', Validators.required],
      recipients: ['All', Validators.required]
    });
  }

  searchUsers(event: Event) {
    const input = event.target as HTMLInputElement;
    const query = input.value.trim();

    if (!query) {
      this.filteredUsers = [];
      return;
    }

    this.http.get<string[]>(`/api/users/search?query=${query}`).subscribe(data => {
      this.filteredUsers = data;
    });
  }

  submit() {
    console.log('clicked');

    const {
      subject,
      description,
      scheduleFromDate,
      scheduleFromTime,
      scheduleToDate,
      scheduleToTime,
      recipients
    } = this.scheduleForm.value;

    console.log('Raw form values:', this.scheduleForm.value);

    // Helper to format datetime as local ISO without UTC shift
    const formatLocalDateTime = (dateStr: string, timeStr: string): string => {
      const [hour, minute] = timeStr.split(':').map(Number);
      const dateObj = new Date(dateStr);
      dateObj.setHours(hour, minute, 0, 0);

      // build string like "2025-08-23T12:12:00"
      const pad = (n: number) => n.toString().padStart(2, '0');
      return (
        dateObj.getFullYear() +
        '-' + pad(dateObj.getMonth() + 1) +
        '-' + pad(dateObj.getDate()) +
        'T' + pad(dateObj.getHours()) +
        ':' + pad(dateObj.getMinutes()) +
        ':' + pad(dateObj.getSeconds())
      );
    };

    // Merge From
    let scheduleFrom: string | null = null;
    if (scheduleFromDate && scheduleFromTime) {
      scheduleFrom = formatLocalDateTime(scheduleFromDate, scheduleFromTime);
    }

    // Merge To
    let scheduleTo: string | null = null;
    if (scheduleToDate && scheduleToTime) {
      scheduleTo = formatLocalDateTime(scheduleToDate, scheduleToTime);
    }

    const payload = { subject, description, recipients, scheduleFrom, scheduleTo };
    console.log('Final payload:', payload);

    this.http.post(`${this.baseUrl}/api/v1/admin/schedule-notification`, payload).subscribe({
      next: () => {
        this.dialogRef.close(payload);
      },
      error: err => console.error(err)
    });
  }


  close() {
    this.dialogRef.close();
  }
}
