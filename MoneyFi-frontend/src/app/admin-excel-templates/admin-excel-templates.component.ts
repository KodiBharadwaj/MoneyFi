import { Component } from '@angular/core';
import { ConfirmLogoutDialogComponent } from '../confirm-logout-dialog/confirm-logout-dialog.component';
import { Router, RouterModule } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { HttpClient } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';
import { environment } from '../../environments/environment';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-admin-excel-templates',
  standalone: true,
  imports: [RouterModule, DatePipe, CommonModule, FormsModule],
  templateUrl: './admin-excel-templates.component.html',
  styleUrl: './admin-excel-templates.component.css'
})
export class AdminExcelTemplatesComponent {

  constructor(private router:Router, private dialog: MatDialog, private httpClient:HttpClient, private toastr: ToastrService) {}
      
  baseUrl = environment.BASE_URL;

  templates: any[] = [];
  selectedFile!: File;
  templateType: string = 'profile-template';
  isUploading = false;
  operationType: string = 'upload';

  ngOnInit() {
    this.getTemplates();
  }

  getTemplates() {
    this.httpClient.get<any[]>(`${this.baseUrl}/api/v1/user-service/admin/get-excel-templates`)
      .subscribe(res => {
        this.templates = res;
      });
  }

  showUploadModal = false;

  openUpload() {
    this.operationType = 'upload';
    this.templateType = '';
    this.showUploadModal = true;
  }

  openUpdate(template: any) {
    this.operationType = 'update';
    this.templateType = 'profile-template';
    this.showUploadModal = true;
  }

  closeUpload() {
    this.showUploadModal = false;
  }

  onFileSelected(event: any) {
    this.selectedFile = event.target.files[0];
  }

  uploadTemplate() {
    this.isUploading = true;

    const formData = new FormData();
    formData.append('type', this.templateType);
    formData.append('operation', this.operationType);
    formData.append('file', this.selectedFile);

    this.httpClient.post(`${this.baseUrl}/api/v1/user-service/admin/excel-template/upload`, formData)
      .subscribe({
        next: () => {
          this.getTemplates();
          this.isUploading = false;
          this.closeUpload();
          this.toastr.success(
            this.operationType === 'upload' ? 'Template uploaded successfully' : 'Template updated successfully'
          );
        },
        error: () => {
          this.isUploading = false;
          this.toastr.error('Operation failed');
        }
      });

  }
  

  downloadTemplate(template: any) {
    const base64Data = template.excelFile;
    const binaryString = window.atob(base64Data);
    const bytes = new Uint8Array(binaryString.length);

    for (let i = 0; i < binaryString.length; i++) {
      bytes[i] = binaryString.charCodeAt(i);
    }

    const blob = new Blob([bytes], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    });

    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = template.excelType + '.xlsx';
    a.click();
    window.URL.revokeObjectURL(url);
  }

  logoutUser(): void {
      const dialogRef = this.dialog.open(ConfirmLogoutDialogComponent, {
        width: '400px',
        panelClass: 'custom-dialog-container',
      });
    
      dialogRef.afterClosed().subscribe((result) => {
        if (result) {
  
          this.httpClient.post(`${this.baseUrl}/api/v1/common/logout`, {}, { responseType: 'text' }).subscribe({
            next: (response) => {
              const jsonResponse = JSON.parse(response);
              if(jsonResponse.message === 'Logged out successfully'){
                  this.toastr.success(jsonResponse.message, '', {
                  timeOut: 1500
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
