import { Component, HostListener, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Subject, Subscription } from 'rxjs';
import { debounceTime, finalize, takeUntil } from 'rxjs/operators';
import { ToastrService } from 'ngx-toastr';
import { AdminService } from '../../services/AdminService';
import { environment } from '../../../environments/environment';

interface HistoryItem {
  status: string;
  time: string;
}

interface UserRequestCount {
  nameChangeActiveRequests?: number;
  nameChangeCompletedRequests?: number;
  nameChangeDeclinedRequests?: number;
  accBlockActiveRequests?: number;
  accBlockChangeCompletedRequests?: number;
  accBlockChangeDeclinedRequests?: number;
  accRetrieveChangeActiveRequests?: number;
  accRetrieveChangeCompletedRequests?: number;
  accRetrieveChangeDeclinedRequests?: number;
}

interface UserProfile {
  username: string;
  name?: string;
  userStatus: string;
  requestId?: number;
  phoneNumber?: string;
  gender?: string;
  dateOfBirth?: string;
  maritalStatus?: string;
  createdTime?: string;
  accountCreationSource?: string;
  address?: string;
  userRequestCount?: UserRequestCount;
  nameChangeRequests?: any[];
  unblockAccountRequests?: any[];
  accountRetrievalRequests?: any[];
  passwordChangeHistoryTrackDtoList?: any[];
  forgotPasswordHistoryTrackDtoList?: any[];
  userDefectTrackingForAdminDtoList?: any[];
  userGmailSyncCountIncreaseRequestHistoryTractDtoLit?: any[];
}

const PLACEHOLDER_IMAGE = 'assets/image-placeholder.png';

@Component({
  selector: 'app-admin-users',
  templateUrl: './admin-users.component.html',
  styleUrls: ['./admin-users.component.css'],
  imports: [CommonModule, FormsModule, RouterModule],
  standalone: true,
})
export class AdminUsersComponent implements OnInit, OnDestroy {
  private readonly apiBase = `${environment.BASE_URL}/api/v1/user-service/admin`;
  private readonly destroy$ = new Subject<void>();
  private readonly search$ = new Subject<{ searchBy: string; value: string }>();

  private usersSub?: Subscription;
  private profileSub?: Subscription;
  private imageSub?: Subscription;

  // Grid state
  status = '';
  users: any[] = [];
  nameFilter = '';
  usernameFilter = '';
  phoneFilter = '';
  isAscending = true;
  isLoading = false; // report generation
  isGridLoading = false;
  search = '';
  searchBy = '';

  // Pagination
  readonly limitOptions = [10, 20, 50, 100];
  totalUsers = 0;
  totalPages = 0;
  currentPage = 1;
  offset = 0;
  limit = 10;

  // Profile dialog
  showUserDialog = false;
  userProfile: UserProfile | null = null;
  profileImage = '';
  isImageLoading = false;
  selectedImage: string | null = null;

  // Block / unblock dialog
  showBlockDialog = false;
  blockReason = '';
  blockSubmitted = false;
  isSubmitting = false;
  selectedFile: File | null = null;

  constructor(
    private route: ActivatedRoute,
    private adminService: AdminService,
    private toastr: ToastrService,
    private httpClient: HttpClient,
  ) {}

  ngOnInit(): void {
    this.route.paramMap.pipe(takeUntil(this.destroy$)).subscribe((params) => {
      this.status = params.get('status') || '';
      this.currentPage = 1;
      this.offset = 0;
      this.fetchUsers();
    });

    // Debounce typing so we don't hit the API on every keystroke.
    this.search$.pipe(debounceTime(300), takeUntil(this.destroy$)).subscribe(({ searchBy, value }) => {
      this.searchBy = searchBy;
      this.search = value;
      this.currentPage = 1;
      this.offset = 0;
      this.fetchUsers();
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.usersSub?.unsubscribe();
    this.profileSub?.unsubscribe();
    this.imageSub?.unsubscribe();
  }

  // ---------------------------------------------------------------- helpers

  trackByUser = (_: number, user: any) => user?.username ?? _;
  trackByIndex = (index: number) => index;

  get actionName(): string {
    return this.userProfile?.userStatus !== 'ACTIVE' ? 'Unblock User' : 'Block User';
  }

  get displayProfileImage(): string {
    return this.profileImage || PLACEHOLDER_IMAGE;
  }

  /** Extracts a readable message from JSON / string / Blob error bodies. */
  private async handleError(err: any, fallback = 'Something went wrong'): Promise<void> {
    let message = fallback;
    try {
      let body = err?.error;
      if (body instanceof Blob) {
        body = await body.text();
      }
      if (typeof body === 'string') {
        body = JSON.parse(body);
      }
      if (body?.message) {
        message = body.message;
      }
    } catch (e) {
      console.error('Failed to parse error response:', err?.error);
    }
    this.toastr.error(message);
  }

  // ------------------------------------------------------------------- grid

  toggleOrder(): void {
    this.isAscending = !this.isAscending;
  }

  onSearch(searchBy: string, value: string): void {
    this.search$.next({ searchBy, value });
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.goToPage(this.currentPage + 1);
    }
  }

  previousPage(): void {
    if (this.currentPage > 1) {
      this.goToPage(this.currentPage - 1);
    }
  }

  changeLimit(): void {
    this.goToPage(1);
  }

  private goToPage(page: number): void {
    this.currentPage = page;
    this.offset = (page - 1) * this.limit;
    this.fetchUsers();
  }

  fetchUsers(): void {
    this.usersSub?.unsubscribe(); // cancel any in-flight request
    this.isGridLoading = true;

    this.usersSub = this.adminService
      .getUsersByStatus(this.status, this.offset, this.limit, this.search, this.searchBy)
      .subscribe({
        next: (res) => {
          this.users = res?.data ?? [];
          this.totalUsers = res?.totalCount ?? 0;
          this.totalPages = Math.ceil(this.totalUsers / this.limit);
          this.isGridLoading = false;
        },
        error: () => {
          this.isGridLoading = false;
          this.toastr.error('Failed to load users');
        },
      });
  }

  generateReport(): void {
    this.isLoading = true;

    this.httpClient
      .get(`${this.apiBase}/user-details/excel`, {
        responseType: 'blob',
        params: {
          status: this.status,
          offset: this.offset,
          limit: this.limit,
          search: this.search,
          searchBy: this.searchBy,
        },
      })
      .pipe(
        finalize(() => (this.isLoading = false)),
        takeUntil(this.destroy$),
      )
      .subscribe({
        next: (response) => {
          const blob = new Blob([response], {
            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
          });
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = 'user_list.xlsx';
          a.click();
          window.URL.revokeObjectURL(url);
        },
        error: (err) => this.handleError(err, 'Failed to generate report'),
      });
  }

  // ---------------------------------------------------------------- profile

  openUserProfile(username: string): void {
    this.userProfile = null;
    this.showUserDialog = true;
    this.loadUserProfile(username);
    this.loadProfileImage(username);
  }

  /** Fetches (or re-fetches) the profile without closing the dialog. */
  private loadUserProfile(username: string): void {
    this.profileSub?.unsubscribe();

    this.profileSub = this.httpClient
      .get<UserProfile>(`${this.apiBase}/user-profile-details`, { params: { username } })
      .subscribe({
        next: (data) => (this.userProfile = this.prepareProfile(data)),
        error: (err) => this.handleError(err, 'Failed to load user profile'),
      });
  }

  /** Pre-computes timeline items once instead of on every change-detection pass. */
  private prepareProfile(profile: UserProfile): UserProfile {
    const withHistory = (list?: any[]) =>
      (list ?? []).map((req) => ({ ...req, historyItems: this.getHistory(req.requestTimeStatusHistory) }));

    return {
      ...profile,
      nameChangeRequests: withHistory(profile.nameChangeRequests),
      unblockAccountRequests: withHistory(profile.unblockAccountRequests),
      accountRetrievalRequests: withHistory(profile.accountRetrievalRequests),
    };
  }

  getHistory(history: any): HistoryItem[] {
    return Object.entries(history ?? {}).map(([key, value]: [string, any]) => ({
      status: key,
      time: value?.requestUpdateTime,
    }));
  }

  private loadProfileImage(username: string): void {
    this.imageSub?.unsubscribe();
    this.profileImage = '';
    this.isImageLoading = true;

    this.imageSub = this.httpClient
      .get(`${this.apiBase}/profile-picture/get`, { params: { username }, responseType: 'blob' })
      .subscribe({
        next: (blob) => {
          if (blob.size === 0) {
            this.isImageLoading = false; // no picture: fall back to placeholder
            return;
          }
          const reader = new FileReader();
          reader.onload = () => {
            this.profileImage = reader.result as string;
            this.isImageLoading = false;
          };
          reader.onerror = () => (this.isImageLoading = false);
          reader.readAsDataURL(blob);
        },
        error: (err) => {
          this.isImageLoading = false;
          console.error('Error fetching profile picture:', err);
        },
      });
  }

  closeUserDialog(): void {
    this.profileSub?.unsubscribe();
    this.imageSub?.unsubscribe();
    this.showUserDialog = false;
    this.userProfile = null;
    this.profileImage = '';
    this.isImageLoading = false;
    this.closeBlockDialog();
  }

  viewImage(username: string | undefined, id: number, type: string): void {
    if (!username) {
      return;
    }

    this.httpClient
      .get(`${this.apiBase}/user-defects/image`, {
        params: { username, type, id },
        responseType: 'blob',
      })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (blob) => {
          const reader = new FileReader();
          reader.onload = () => (this.selectedImage = reader.result as string);
          reader.readAsDataURL(blob);
        },
        error: (err) => this.handleError(err),
      });
  }

  // ------------------------------------------------------- block / unblock

  openBlockDialog(): void {
    this.showBlockDialog = true;
  }

  closeBlockDialog(): void {
    this.showBlockDialog = false;
    this.blockReason = '';
    this.blockSubmitted = false;
    this.selectedFile = null;
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFile = input.files?.[0] ?? null;
  }

  blockOrUnblockUser(requestId?: number): void {
    if (this.isSubmitting || !this.userProfile) {
      return;
    }

    this.blockSubmitted = true;
    if (!this.blockReason.trim() || !this.selectedFile) {
      return; // inline validation messages are shown in the template
    }

    const username = this.userProfile.username;
    const isBlocking = this.userProfile.userStatus === 'ACTIVE';

    const formData = new FormData();
    formData.append('email', username);
    formData.append('reason', this.blockReason.trim());
    formData.append('requestId', requestId?.toString() || '');
    formData.append('type', isBlocking ? 'BLOCK' : 'UNBLOCK');
    formData.append('file', this.selectedFile);

    this.isSubmitting = true;

    this.httpClient
      .post(`${this.apiBase}/user-accounts/status`, formData)
      .pipe(
        finalize(() => (this.isSubmitting = false)),
        takeUntil(this.destroy$),
      )
      .subscribe({
        next: () => {
          this.toastr.success(isBlocking ? 'User blocked successfully' : 'User unblocked successfully');
          this.closeBlockDialog();
          this.loadUserProfile(username); // refresh status badge, button text and request history
          this.fetchUsers(); // keep the grid in sync
        },
        error: (err) => this.handleError(err),
      });
  }

  // --------------------------------------------------------------- keyboard

  /** Esc closes the top-most layer: image → block dialog → profile dialog. */
  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.selectedImage) {
      this.selectedImage = null;
    } else if (this.showBlockDialog) {
      if (!this.isSubmitting) {
        this.closeBlockDialog();
      }
    } else if (this.showUserDialog) {
      this.closeUserDialog();
    }
  }
}