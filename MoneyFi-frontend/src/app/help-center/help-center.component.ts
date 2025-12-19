import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { environment } from '../../environments/environment';
import { CommonModule } from '@angular/common';
import { NgChartsModule } from 'ng2-charts';
import { FormsModule } from '@angular/forms';
import e from 'express';

@Component({
  selector: 'app-help-center',
  standalone: true,
  imports: [CommonModule,FormsModule,RouterModule,NgChartsModule],
  templateUrl: './help-center.component.html',
  styleUrl: './help-center.component.css'
})
export class HelpCenterComponent {
  formData = {
      email: '',
      phoneNumber: '',
      name: '',
      description: ''
    };
  
    constructor(
      private http: HttpClient,
      private router: Router,
      private toastr: ToastrService
    ) {}
  
    baseUrl = environment.BASE_URL; 
  
    navigateTo(route: string): void {
      this.router.navigate([route]);
    }
  
    isLoading: boolean = false;
  
    submitForm(form: any): void {
      this.isLoading = true;
      this.http.post(`${this.baseUrl}/api/v1/user-service/open/contact-us`, this.formData).subscribe({
        next: (response: any) => {
          this.isLoading = false;
          this.toastr.success('Details sumbitted to admin');
          form.resetForm();
        }, error: (err) => {
            console.error(err);
            this.isLoading = false;
            try {
                const errorObj = typeof err.error === 'string' ? JSON.parse(err.error) : err.error;
                this.toastr.error(errorObj.message);
              } catch (e) {
                console.error('Failed to parse error:', err.error);
              }
          },
      });
    }
}
