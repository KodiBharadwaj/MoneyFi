import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ToastrService } from 'ngx-toastr';
import { ConfirmLogoutDialogComponent } from '../confirm-logout-dialog/confirm-logout-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';

type SupportRole = 'ADMIN' | 'CUSTOMER_CARE';

interface AdminUsersResponseDto {
  id: number;
  username: string;
  isRegistered?: boolean;
}

@Component({
  selector: 'app-maintainer-home',
  imports: [FormsModule, CommonModule],
  standalone: true,
  templateUrl: './maintainer-home.component.html',
  styleUrl: './maintainer-home.component.css'
})
export class MaintainerHomeComponent implements OnInit {

  readonly roleTabs: { value: SupportRole; label: string }[] = [
    { value: 'ADMIN', label: 'Admin' },
    { value: 'CUSTOMER_CARE', label: 'Customer Care' }
  ];
  selectedRole: SupportRole = 'ADMIN';

  get roleLabel(): string {
    return this.roleTabs.find(t => t.value === this.selectedRole)!.label;
  }

  admins: AdminUsersResponseDto[] = [];
  blockedAdmins: any[] = [];
  deletedAdmins: any[] = [];
  filteredAdmins: AdminUsersResponseDto[] = [];
  searchText: string = '';

  showAddModal = false;
  showUpdateModal = false;
  selectedAdminId: number | null = null;

  addForm = {
    username: '',
    password: '',
    comment: '',
    role: 'ADMIN' as SupportRole
  };

  updateForm = {
    username: '',
    password: '',
    comment: '',
    role: 'ADMIN' as SupportRole
  };

  constructor(private http: HttpClient, private toastr: ToastrService, private dialog: MatDialog,
    private httpClient: HttpClient, private router:Router) {}

  baseUrl = environment.BASE_URL;

  ngOnInit(): void {
    this.loadAll();
  }

  /* ================= ROLE TABS ================= */
  selectRole(role: SupportRole) {
    if (this.selectedRole === role) return;
    this.selectedRole = role;
    this.searchText = '';
    this.admins = [];
    this.filteredAdmins = [];
    this.blockedAdmins = [];
    this.deletedAdmins = [];
    this.loadAll();
  }

  private loadAll() {
    this.getAdmins();
    this.getBlockedAdmins();
    this.getDeletedAdmins();
  }

  private showError(err: any) {
    try {
      const errorObj = typeof err.error === 'string' ? JSON.parse(err.error) : err.error;
      this.toastr.error(errorObj.message);
    } catch (e) {
      console.error('Failed to parse error:', err.error);
    }
  }

  getAdmins() {
    const role = this.selectedRole;
    this.http.get<AdminUsersResponseDto[]>(`${this.baseUrl}/api/v1/user-service/maintainer/support-users/get?type=ACTIVE&role=${role}`)
      .subscribe(res => {
        if (role !== this.selectedRole) return; // tab switched before response arrived
        this.admins = res || [];
        this.applySearch();
      });
  }

  applySearch() {
    const value = this.searchText.toLowerCase();
    this.filteredAdmins = this.admins.filter(a =>
      a.username.toLowerCase().includes(value)
    );
  }

  /* ================= ADD USER ================= */
  openAddAdminModal() {
    this.addForm = { username: '', password: '', comment: '', role: this.selectedRole };
    this.showAddModal = true;
  }

  closeAddModal() {
    this.showAddModal = false;
    this.addForm = { username: '', password: '', comment: '', role: this.selectedRole };
  }

  createAdmin() {
    if (!this.addForm.username || !this.addForm.password) {
      alert('Username and Password are required');
      return;
    }

    this.http.post(`${this.baseUrl}/api/v1/user-service/maintainer/support-user/add`, this.addForm).subscribe({
      next: () => {
        this.closeAddModal();
        this.getAdmins();
      },
      error: (err) => this.showError(err)
    });
  }

  /* ================= UPDATE USER ================= */
  openUpdateModal(admin: AdminUsersResponseDto) {
    this.selectedAdminId = admin.id;
    this.updateForm = { username: admin.username, password: '', comment: '', role: this.selectedRole };
    this.showUpdateModal = true;
  }

  closeUpdateModal() {
    this.showUpdateModal = false;
    this.selectedAdminId = null;
  }

  updateAdmin() {
    if (!this.updateForm.username || !this.updateForm.password) {
      alert('Username and Password are required');
      return;
    }

    this.http.post(`${this.baseUrl}/api/v1/user-service/maintainer/${this.selectedAdminId}/support-user/update`,
      this.updateForm).subscribe({
      next: () => {
        this.closeUpdateModal();
        this.getAdmins();
      },
      error: (err) => this.showError(err)
    });
  }

  blockOrDeleteAdmin(admin: AdminUsersResponseDto, type: String) {
    this.http.delete(`${this.baseUrl}/api/v1/user-service/maintainer/${admin.id}/delete-support-user?type=${type}`).subscribe({
      next: () => this.loadAll(),
      error: (err) => this.showError(err)
    });
  }

  registeringAgentId: number | null = null;

  registerAgent(admin: AdminUsersResponseDto) {
    this.registeringAgentId = admin.id;
    this.http.get(`${this.baseUrl}/api/v1/user-service/maintainer/${admin.id}/register-agent`, { responseType: 'text' })
      .subscribe({
        next: () => {
          this.registeringAgentId = null;
          this.toastr.success('Registered as agent');
          this.getAdmins();
        },
        error: (err) => {
          this.registeringAgentId = null;
          this.showError(err);
        }
      });
  }

  getBlockedAdmins() {
    const role = this.selectedRole;
    this.http.get<AdminUsersResponseDto[]>(`${this.baseUrl}/api/v1/user-service/maintainer/support-users/get?type=BLOCK&role=${role}`)
      .subscribe(res => {
        if (role !== this.selectedRole) return;
        this.blockedAdmins = res || [];
      });
  }

  getDeletedAdmins() {
    const role = this.selectedRole;
    this.http.get<AdminUsersResponseDto[]>(`${this.baseUrl}/api/v1/user-service/maintainer/support-users/get?type=DELETE&role=${role}`)
      .subscribe(res => {
        if (role !== this.selectedRole) return;
        this.deletedAdmins = res || [];
      });
  }

  unblockOrUnDeleteAdmin(admin: any, type: string) {
    this.http.post(`${this.baseUrl}/api/v1/user-service/maintainer/${admin.id}/retrieve-support-user?type=${type}`, {})
      .subscribe({
        next: () => this.loadAll(),
        error: (err) => this.showError(err)
      });
  }

  isLogoutLoading = false;
  logoutUser(): void {
      const dialogRef = this.dialog.open(ConfirmLogoutDialogComponent, {
        width: '400px',
        panelClass: 'custom-dialog-container',
      });
      dialogRef.afterClosed().subscribe((result) => {
        if (result) {
          this.isLogoutLoading = true;
          this.httpClient.post(`${this.baseUrl}/api/v1/user-service/common/logout`, {}, { responseType: 'text' }).subscribe({
            next: (response) => {
              this.isLogoutLoading = false;
              const jsonResponse = JSON.parse(response);
              if(jsonResponse.message === 'Logged out successfully'){
                  this.toastr.success(jsonResponse.message, '', {
                  timeOut: 1500  // time in milliseconds (3 seconds)
                });
                sessionStorage.removeItem('moneyfi.auth');
                sessionStorage.clear();
                this.router.navigate(['']);
              }
              else {
                this.toastr.error('Failed to logout')
              }
            },
            error: (error) => {
              this.isLogoutLoading = false;
              console.error(error);
              this.toastr.error('Failed to logout')
            }
          });
        }
      });
    }
}
