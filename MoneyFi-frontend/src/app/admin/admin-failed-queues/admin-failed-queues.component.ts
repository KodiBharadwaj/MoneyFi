import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

interface FailedQueueDetailsResponseDto {
  failedQueueId: number;
  mdcRequestId: string;
  referenceNumber: string;
  username: string;
  userId: number;
  name: string;
  message: string;
  failureTime: string;
}

@Component({
  selector: 'app-admin-failed-queues',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-failed-queues.component.html',
  styleUrl: './admin-failed-queues.component.css'
})
export class AdminFailedQueuesComponent implements OnInit {

  constructor(private httpClient: HttpClient) {}

  baseUrl = environment.BASE_URL;

  // datetime-local values (yyyy-MM-ddTHH:mm:ss)
  startDate = '';
  endDate = '';

  failedQueues: FailedQueueDetailsResponseDto[] = [];
  loading = false;
  errorMessage = '';
  expandedId: number | null = null;

  offset = 0;
  limit = 10;
  totalCount = 0;

  ngOnInit(): void {
    const start = new Date();
    start.setHours(0, 0, 0, 0);
    const end = new Date();
    end.setHours(23, 59, 59, 0);
    this.startDate = this.toLocalInput(start);
    this.endDate = this.toLocalInput(end);
    this.fetchFailedQueues();
  }

  private pad(n: number): string {
    return n.toString().padStart(2, '0');
  }

  private toLocalInput(d: Date): string {
    return `${d.getFullYear()}-${this.pad(d.getMonth() + 1)}-${this.pad(d.getDate())}` +
      `T${this.pad(d.getHours())}:${this.pad(d.getMinutes())}:${this.pad(d.getSeconds())}`;
  }

  // Converts a local datetime-local value to ISO-8601 with the local offset, e.g. 2026-10-28T00:00:00+05:30
  private toOffsetIso(value: string): string {
    const withSeconds = value.length === 16 ? `${value}:00` : value;
    const d = new Date(withSeconds);
    const offsetMin = -d.getTimezoneOffset();
    const sign = offsetMin >= 0 ? '+' : '-';
    const abs = Math.abs(offsetMin);
    return `${this.toLocalInput(d)}${sign}${this.pad(Math.floor(abs / 60))}:${this.pad(abs % 60)}`;
  }

  search() {
    this.offset = 0;
    this.fetchFailedQueues();
  }

  fetchFailedQueues() {
    if (!this.startDate || !this.endDate) {
      this.errorMessage = 'Please select both start and end date.';
      return;
    }
    if (new Date(this.startDate) > new Date(this.endDate)) {
      this.errorMessage = 'Start date cannot be after end date.';
      return;
    }

    this.errorMessage = '';
    this.loading = true;
    // '+' in the offset must be percent-encoded, so build the query manually
    const url = `${this.baseUrl}/api/v1/user-service/admin/failed-queues` +
      `?startDate=${encodeURIComponent(this.toOffsetIso(this.startDate))}` +
      `&endDate=${encodeURIComponent(this.toOffsetIso(this.endDate))}` +
      `&offset=${this.offset}&limit=${this.limit}`;

    this.httpClient.get<{ data: FailedQueueDetailsResponseDto[]; totalCount: number }>(url).subscribe({
      next: (res) => {
        this.failedQueues = res.data ?? [];
        this.totalCount = res.totalCount ?? 0;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching failed queues', err);
        this.failedQueues = [];
        this.totalCount = 0;
        this.errorMessage = 'Failed to load failed queue details.';
        this.loading = false;
      }
    });
  }

  toggleRow(id: number) {
    this.expandedId = this.expandedId === id ? null : id;
  }

  nextPage() {
    if (this.offset + this.limit < this.totalCount) {
      this.offset += this.limit;
      this.fetchFailedQueues();
    }
  }

  previousPage() {
    if (this.offset > 0) {
      this.offset = Math.max(0, this.offset - this.limit);
      this.fetchFailedQueues();
    }
  }

  changeLimit() {
    this.limit = Number(this.limit);
    this.offset = 0;
    this.fetchFailedQueues();
  }

  get startRecord(): number {
    return this.totalCount === 0 ? 0 : this.offset + 1;
  }

  get endRecord(): number {
    return Math.min(this.offset + this.limit, this.totalCount);
  }
}
