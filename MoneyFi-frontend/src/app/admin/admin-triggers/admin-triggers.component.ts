import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-admin-triggers',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-triggers.component.html',
  styleUrl: './admin-triggers.component.css'
})
export class AdminTriggersComponent {

  loading: boolean = false;
  message: string = '';

  baseUrl = environment.BASE_URL;

  triggerBatch(type: string) {

    this.loading = true;
    this.message = '';

    const token = sessionStorage.getItem('moneyfi.auth');

    fetch(`${this.baseUrl}/api/v1/transaction/admin/batch-sync?type=${type}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
      .then(response => {

        if (!response.ok) {
          throw new Error('Failed to trigger batch');
        }

        this.message = `${type} batch triggered successfully`;
      })
      .catch(error => {
        console.error(error);
        this.message = `Failed to trigger ${type} batch`;
      })
      .finally(() => {
        this.loading = false;
      });
  }
}
