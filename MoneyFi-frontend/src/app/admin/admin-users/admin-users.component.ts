import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { AdminService } from '../../services/AdminService';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-admin-users',
  templateUrl: './admin-users.component.html',
  styleUrls: ['./admin-users.component.css'],
  imports : [CommonModule, FormsModule, RouterModule],
  standalone : true
})
export class AdminUsersComponent implements OnInit, OnDestroy {
  status: string = '';
  users: any[] = [];

  nameFilter: string = '';
  usernameFilter: string = '';
  phoneFilter: string = '';
  isAscending: boolean = true;
  isLoading = false;
  isGridLoading = false;


  constructor(private router: ActivatedRoute, private adminService: AdminService, private toastr:ToastrService, private httpClient:HttpClient,
  ) {}

  baseUrl = environment.BASE_URL;

  ngOnInit(): void {
    this.router.paramMap.subscribe(params => {
      this.status = params.get('status') || '';
      this.fetchUsers(this.status);
    });
  }

  fetchUsers(status: string) {
    this.isGridLoading = true;
    this.adminService.getUsersByStatus(status).subscribe({
      next : (data) => {
      this.users = data;
      this.isGridLoading = false;
    },
      error: (err) => {
        console.error('Failed to fetch users', err);
        this.isGridLoading = false;
      }
    });
  }

  // Filter function
  filteredUsers() {
    let filtered = this.users.filter(user =>
      (!this.nameFilter || user.name?.toLowerCase().includes(this.nameFilter.toLowerCase())) &&
      (!this.usernameFilter || user.username?.toLowerCase().includes(this.usernameFilter.toLowerCase())) &&
      (!this.phoneFilter || user.phone?.toString().includes(this.phoneFilter))
    );

    return this.isAscending
      ? filtered
      : [...filtered].reverse();
  }


  // Toggle order
  toggleOrder() {
    this.isAscending = !this.isAscending;
  }

  showUserDialog=false;
  userProfile:any;
  profileImageUrl:any;

  openUserProfile(username:string){

    this.showUserDialog=true;

    this.httpClient.get(`${this.baseUrl}/api/v1/user-service/admin/user-profile-details?username=${username}`)
    .subscribe((data:any)=>{
        this.userProfile=data;
    });

    this.loadProfileImage(username);
  }

  profileImage = '';
  isImageLoading: boolean = false;
  loadProfileImage(username:string): void {
    this.isImageLoading = true;

    this.httpClient.get(`${this.baseUrl}/api/v1/user-service/admin/profile-picture/get?username=${username}`, { responseType: 'blob' })
      .subscribe({
        next: (blob) => {
          if (blob.size > 0) {
            const reader = new FileReader();
            reader.onload = (e: any) => {
              this.profileImage = e.target.result; // base64 string
              sessionStorage.setItem('moneyfi.user.profile.image', this.profileImage);
              this.isImageLoading = false;
            };
            reader.readAsDataURL(blob);
          } else {
            console.warn('No profile picture found.');
          }
        },
        error: (err) => {
          this.isImageLoading = false;
          console.error('Error fetching profile picture:', err);
        }
      });
  }

  closeUserDialog(){
    this.showUserDialog=false;
    this.profileImage = '';
  }

  getHistory(history: any) {
    return Object.entries(history).map(([key, value]: any) => ({
      status: key,
      time: value.requestUpdateTime
    }));
  }

  selectedImage: string | null = null;
  viewImage(username: string, defectId: number, type:string): void {
    this.httpClient.get(`${this.baseUrl}/api/v1/user-service/admin/user-defects/image?username=${username}&type=${type}&id=${defectId}`, { responseType: 'blob' })
      .subscribe({
        next: (blob) => {
          const reader = new FileReader();
          reader.onload = () => {
            this.selectedImage = reader.result as string;
          };
          reader.readAsDataURL(blob);
        },
        error: (err) => {
          if (err.error instanceof Blob) {
            const reader = new FileReader();
            reader.onload = () => {
              try {
                const text = reader.result as string;
                const errorObj = JSON.parse(text);
                this.toastr.error(errorObj.message);
              } catch (e) {
                console.error('Failed to parse Blob error:', e);
                this.toastr.error('Something went wrong');
              }
            };
            reader.readAsText(err.error);
          } else {
            // fallback if it's not a Blob
            const message = err.error?.message || 'Something went wrong';
            this.toastr.error(message);
          }
        }
      });
  }


  showBlockDialog = false;
  blockReason = '';

  openBlockDialog(){
    this.showBlockDialog = true;
  }

  closeBlockDialog(){
    this.showBlockDialog = false;
  }


  selectedFile!: File;
  fileError = false;
  onFileSelected(event:any){
    const file = event.target.files[0];

    if(file){
      this.selectedFile = file;
      this.fileError = false;
    }
  }

  blockUser(){
    if(!this.blockReason || !this.selectedFile){
      this.fileError = !this.selectedFile;
      alert("Please provide reason and upload evidence file");
      return;
    }
    const formData = new FormData();
    formData.append("email", this.userProfile.username);
    formData.append("reason", this.blockReason);

    if(this.selectedFile){
      formData.append("file", this.selectedFile);
    }

    this.httpClient.post(`${this.baseUrl}/api/v1/user-service/admin/user-account/block`, formData ).subscribe({
        next: (res)=>{
          this.toastr.success('User Blocked Successfully');
          this.closeBlockDialog();
        },
        error: (err)=>{
          try {
            const errorObj = typeof err.error === 'string' ? JSON.parse(err.error) : err.error;
            this.toastr.error(errorObj.message);
          } catch (e) {
            console.error('Failed to parse error:', err.error);
          }
        }
    });
  }

  generateReport() {
    this.isLoading = true;
    this.httpClient.get(`${this.baseUrl}/api/v1/user-service/admin/user-details/excel?status=${this.status}`, { responseType: 'blob' }).subscribe({
      next: (response) => {
        // Trigger File Download
        const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `user_list.xlsx`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
        this.isLoading = false;
      },
      error: (error) => {
        this.isLoading = false;
        console.error('Failed to generate report:', error);
        // alert("Failed to generate the report. Please try again.");
        console.log(error.status)
        console.log(error.error?.message);

        if(error.status === 401){
            if (error.error === 'TokenExpired') {
              alert('Your session has expired. Please login again.');
              sessionStorage.removeItem('moneyfi.auth');
              // this.route.navigate(['/']);
            } else if(error.error === 'Token is blacklisted'){
              alert('Your session has expired. Please login again.');
              sessionStorage.removeItem('moneyfi.auth');
              // this.route.navigate(['/']);
            }
            else if(error.error === 'AuthorizationFailed'){
              alert('Service Unavailable!! Please try later');
            }
          } else if (error.status === 503){
            alert('Service Unavailable!! Please try later');
          } else if (error.status === 404 && error?.message === 'No user data found to generate excel'){
            this.toastr.error('Failed to generate report due to no data');
          }
      }
    });
  }

  ngOnDestroy(): void {
    this.closeUserDialog();
  }
}
