import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

interface GmailSyncHistoryResponse {
  syncTime: string;
  syncCount: number;
}

@Component({
  selector: 'app-gmail-sync-calendar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './gmail-sync-calendar.component.html',
  styleUrl: './gmail-sync-calendar.component.css'
})
export class GmailSyncCalendarComponent {

  @Input() history: GmailSyncHistoryResponse[] = [];
  @Output() dateSelected = new EventEmitter<Date>();

  currentMonth = new Date();
  weekDays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
  
  months = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ];

  // Navigate to previous month
  previousMonth() {
    this.currentMonth = new Date(
      this.currentMonth.getFullYear(),
      this.currentMonth.getMonth() - 1,
      1
    );
  }

  // Navigate to next month
  nextMonth() {
    this.currentMonth = new Date(
      this.currentMonth.getFullYear(),
      this.currentMonth.getMonth() + 1,
      1
    );
  }

  // Go to today
  goToToday() {
    this.currentMonth = new Date();
  }

  // Get current month name
  get currentMonthName(): string {
    return this.months[this.currentMonth.getMonth()];
  }

  // Get current year
  get currentYear(): number {
    return this.currentMonth.getFullYear();
  }

  // Get calendar grid with padding days
  get calendarDays(): (Date | null)[] {
    const year = this.currentMonth.getFullYear();
    const month = this.currentMonth.getMonth();
    
    // First day of the month
    const firstDay = new Date(year, month, 1);
    const firstDayOfWeek = firstDay.getDay();
    
    // Last day of the month
    const lastDay = new Date(year, month + 1, 0);
    const lastDate = lastDay.getDate();
    
    const days: (Date | null)[] = [];
    
    // Add empty slots for days before the first day of month
    for (let i = 0; i < firstDayOfWeek; i++) {
      days.push(null);
    }
    
    // Add all days of the month
    for (let date = 1; date <= lastDate; date++) {
      days.push(new Date(year, month, date));
    }
    
    return days;
  }

  // Check if a date is today
  isToday(day: Date | null): boolean {
    if (!day) return false;
    const today = new Date();
    return day.toDateString() === today.toDateString();
  }

  // Get history for a specific day
  getHistoryForDay(day: Date | null): GmailSyncHistoryResponse | undefined {
    if (!day) return undefined;
    return this.history.find(h =>
      new Date(h.syncTime).toDateString() === day.toDateString()
    );
  }

  // Handle day selection
  selectDay(day: Date | null) {
    if (!day) return;
    
    // Allow clicking any valid date (both synced and non-synced)
    this.dateSelected.emit(day);
  }

  // Check if date has synced data
  hasSyncData(day: Date | null): boolean {
    return !!this.getHistoryForDay(day);
  }
}