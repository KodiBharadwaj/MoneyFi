import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { environment } from '../../../environments/environment';
import { ToastrService } from 'ngx-toastr';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-admin-categories-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './admin-categories-list.component.html',
  styleUrl: './admin-categories-list.component.css'
})
export class AdminCategoriesListComponent {

  constructor(private http: HttpClient, private toasr: ToastrService) {};

  categoryTypes: string[] = [];
  selectedType = '';
  newCategory = '';
  categories: any[] = [];
  baseUrl = environment.BASE_URL;

  ngOnInit() {
    this.http.get<string[]>(`${this.baseUrl}/api/v1/wealth-core/admin/category-type/get`)
      .subscribe(res => this.categoryTypes = res);
  }

  onTypeChange() {
    this.http.post<any[]>(
      `${this.baseUrl}/api/v1/wealth-core/common/category-list/get`,
      [this.selectedType]
    ).subscribe(res => {
      this.categories = res.map(item => ({
        ...item,
        editing: false
      }));
    });
  }

  addCategory() {
    if (!this.newCategory.trim()) return;

    this.http.post(
      `${this.baseUrl}/api/v1/wealth-core/admin/category-list/save`,
      {
        type: this.selectedType,
        category: this.newCategory
      }
    ).subscribe(() => {
      this.newCategory = '';
      this.onTypeChange();
    });
  }

  enableEdit(item: any) {
    item.editing = true;
    item.original = item.category;
  }

  cancelEdit(item: any) {
    item.category = item.original;
    item.editing = false;
  }

  updateCategory(item: any) {
    this.http.put(
      `${this.baseUrl}/api/v1/wealth-core/admin/category-list/${item.categoryId}/update`,
      {
        type: this.selectedType,
        category: item.category
      }
    ).subscribe(() => item.editing = false);
  }

  deleteCategory(item: any) {
    if (!confirm('Delete this category?')) return;

    this.http.delete(`${this.baseUrl}/api/v1/wealth-core/admin/category-list/${item.categoryId}/delete`).subscribe({
      next: () => {
        this.onTypeChange()
      },
      error: (err) => {
        this.toasr.error('Failed to delete');
      }
    });
  }
}