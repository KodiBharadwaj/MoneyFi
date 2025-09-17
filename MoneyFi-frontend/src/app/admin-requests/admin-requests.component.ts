import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AdminService } from '../services/AdminService';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { MatDialog } from '@angular/material/dialog';
import { AdminRequestDialogComponent } from '../admin-request-dialog/admin-request-dialog.component';
import { ConfirmLogoutDialogComponent } from '../confirm-logout-dialog/confirm-logout-dialog.component';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { AdminRequestDeclineDialogComponent } from '../admin-request-decline-dialog/admin-request-decline-dialog.component';

@Component({
  selector: 'app-admin-requests',
  imports : [CommonModule, FormsModule, RouterModule],
    standalone : true,
  templateUrl: './admin-requests.component.html',
  styleUrl: './admin-requests.component.css'
})
export class AdminRequestsComponent implements OnInit {

  constructor(private route: ActivatedRoute, private adminService: AdminService,
      private toastr: ToastrService, private dialog: MatDialog, private httpClient : HttpClient, private router:Router
  ) {}

  baseUrl = environment.BASE_URL;

  status: string = '';
  requestType: string = '';
  requests: any[] = [];
  isGridLoading = false;

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this.status = params.get('status') || '';
      this.fetchUsers(this.status);
    });
  }

  fetchUsers(status: string) {
    this.isGridLoading = true;
    this.adminService.getUserRequestsByStatus(status).subscribe({
      next : (data) => {
      this.requests = data;
      this.isGridLoading = false;
    },
      error: (err) => {
        console.error('Failed to fetch requests', err);
        this.isGridLoading = false;
      }
    })
  }

  approveUserRequest(request: any, status: string): void {
    if(status === 'Rename') this.requestType = 'NAME_CHANGE_REQUEST';
    else if(status === 'Unblock') this.requestType = 'ACCOUNT_UNBLOCK_REQUEST';
    else if(status === 'Retrieve') this.requestType = 'ACCOUNT_NOT_DELETE_REQUEST';
    else if(status === 'All'){
      if(request.requestType === 'Account Unblock') this.requestType = 'ACCOUNT_UNBLOCK_REQUEST';
      else if(request.requestType === 'Name Change') this.requestType = 'NAME_CHANGE_REQUEST';
      else if(request.requestType === 'Account Retrieval') this.requestType = 'ACCOUNT_NOT_DELETE_REQUEST';
    }

    const dialogRef = this.dialog.open(AdminRequestDialogComponent, {
      width: '400px',
      data: {
        name: request.name,
        username: request.username,
        description: request.description,
        referenceNumber: request.referenceNumber,
        requestType : this.requestType
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === 'approved') {
        this.toastr.success('Request has been approved');
        this.fetchUsers(status);
      }
    });
  }

  declineUserRequest(request: any, status: string): void {
    console.log(status)
    if(status === 'Rename') this.requestType = 'NAME_CHANGE_REQUEST';
    else if(status === 'Unblock') this.requestType = 'ACCOUNT_UNBLOCK_REQUEST';
    else if(status === 'Retrieve') this.requestType = 'ACCOUNT_NOT_DELETE_REQUEST';

    const dialogRef = this.dialog.open(AdminRequestDeclineDialogComponent, {
      width: '400px',
      data: {
        name: request.name,
        username: request.username,
        description: request.description,
        referenceNumber: request.referenceNumber,
        requestType : this.requestType
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === 'declined') {
        this.toastr.success('Request has been declined');
        this.fetchUsers(status);
      }
    });
  }

  copyToClipboard(text: string) {
    navigator.clipboard.writeText(text).then(() => {
      this.toastr.success("Copied to clipboard")
    }).catch(() => {
      this.toastr.error('Failed to copy!');
    });
  }

  logoutUser(): void {
    const dialogRef = this.dialog.open(ConfirmLogoutDialogComponent, {
      width: '400px',
      panelClass: 'custom-dialog-container',
    });
  
    dialogRef.afterClosed().subscribe((result) => {
      if (result) {

        this.httpClient.post(`${this.baseUrl}/api/v1/admin/logout`, {}, { responseType: 'text' }).subscribe({
          next: (response) => {
            const jsonResponse = JSON.parse(response);
            if(jsonResponse.message === 'Logged out successfully'){
                this.toastr.success(jsonResponse.message, '', {
                timeOut: 1500  // time in milliseconds (3 seconds)
              });
              sessionStorage.removeItem('moneyfi.auth');
              this.router.navigate(['admin/login']);
            } 
            else {
              this.toastr.error('Failed to logout')
            }
          },
          error: (error) => {
            console.error(error);
            this.toastr.error('Failed to logout')
          }
        });
      }
    });
  }

}
