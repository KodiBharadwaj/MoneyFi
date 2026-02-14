import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ToastrService } from 'ngx-toastr';

interface AdminUsersResponseDto {
  id: number;
  username: string;
}

@Component({
  selector: 'app-maintainer-home',
  imports: [FormsModule, CommonModule],
  standalone: true,
  templateUrl: './maintainer-home.component.html',
  styleUrl: './maintainer-home.component.css'
})
export class MaintainerHomeComponent implements OnInit {

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
    comment: ''
  };

  updateForm = {
    username: '',
    password: '',
    comment: ''
  };

  constructor(private http: HttpClient, private toastr: ToastrService) {}

  baseUrl = environment.BASE_URL;

  ngOnInit(): void {
    this.getAdmins();
    this.getBlockedAdmins();  
    this.getDeletedAdmins();  
  }

  getAdmins() {
    this.http.get<AdminUsersResponseDto[]>(`${this.baseUrl}/api/v1/maintainer/get-admins?type=ACTIVE`)
      .subscribe(res => {
        this.admins = res;
        this.filteredAdmins = res;
      });
  }

  applySearch() {
    const value = this.searchText.toLowerCase();
    this.filteredAdmins = this.admins.filter(a =>
      a.username.toLowerCase().includes(value)
    );
  }

  /* ================= ADD ADMIN ================= */
  openAddAdminModal() {
    this.showAddModal = true;
  }

  closeAddModal() {
    this.showAddModal = false;
    this.addForm = { username: '', password: '', comment: '' };
  }

  createAdmin() {
    if (!this.addForm.username || !this.addForm.password) {
      alert('Username and Password are required');
      return;
    }

    this.http.post(`${this.baseUrl}/api/v1/maintainer/add-admin`, this.addForm).subscribe({
      next: (data) => {
        this.closeAddModal();
        this.getAdmins(); // refresh list
      },
      error: (err) => {
        try {
          const errorObj = typeof err.error === 'string' ? JSON.parse(err.error) : err.error;
          this.toastr.error(errorObj.message);
        } catch (e) {
          console.error('Failed to parse error:', err.error);
        }
      }
    });
  }

  /* ================= UPDATE ADMIN ================= */
  openUpdateModal(admin: AdminUsersResponseDto) {
    this.selectedAdminId = admin.id;
    this.updateForm.username = admin.username;
    this.updateForm.password = '';
    this.updateForm.comment = '';
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

    this.http.post(
      `${this.baseUrl}/api/v1/maintainer/${this.selectedAdminId}/update-admin`,
      this.updateForm
    ).subscribe(() => {
      this.closeUpdateModal();
      this.getAdmins(); // refresh list
    });

    this.http.post(`${this.baseUrl}/api/v1/maintainer/${this.selectedAdminId}/update-admin`,
      this.updateForm).subscribe({
      next: (data) => {
        this.closeUpdateModal();
        this.getAdmins(); // refresh list
      },
      error: (err) => {
        try {
          const errorObj = typeof err.error === 'string' ? JSON.parse(err.error) : err.error;
          this.toastr.error(errorObj.message);
        } catch (e) {
          console.error('Failed to parse error:', err.error);
        }
      }
    });
  }

  blockOrDeleteAdmin(admin: AdminUsersResponseDto, type: String) {
    this.http.delete(`${this.baseUrl}/api/v1/maintainer/${admin.id}/delete-admin?type=${type}`).subscribe({
      next: (data) => {
        this.getAdmins();
        this.getBlockedAdmins();  
        this.getDeletedAdmins();
      },
      error: (err) => {
        try {
          const errorObj = typeof err.error === 'string' ? JSON.parse(err.error) : err.error;
          this.toastr.error(errorObj.message);
        } catch (e) {
          console.error('Failed to parse error:', err.error);
        }
      }
    });
  }

  getBlockedAdmins() {
    this.http.get<AdminUsersResponseDto[]>(`${this.baseUrl}/api/v1/maintainer/get-admins?type=BLOCK`)
    .subscribe(res => {
      this.blockedAdmins = res || [];
    });
  }

  getDeletedAdmins() {
    this.http.get<AdminUsersResponseDto[]>(`${this.baseUrl}/api/v1/maintainer/get-admins?type=DELETE`)
      .subscribe(res => {
        this.deletedAdmins = res || [];
      });
  }

  unblockOrUnDeleteAdmin(admin: any, type: string) {
    this.http.post(`${this.baseUrl}/api/v1/maintainer/${admin.id}/retrieve-admin?type=${type}`, {})
      .subscribe(() => {
        this.getAdmins();
        this.getBlockedAdmins();
      });
  }
}
