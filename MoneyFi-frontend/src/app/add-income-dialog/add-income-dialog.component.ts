import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { FormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';
import { environment } from '../../environments/environment';
import { CategoryService } from '../services/category.service';
import { Category } from '../model/category-list';
import { Router } from '@angular/router';

interface IncomeSource {
  id: number;
  source: string;
  amount: number;
  date: string;
  categoryId: number;
  recurring: boolean;
  is_deleted: boolean;
  description: string;
}
@Component({
  selector: 'app-add-income-dialog',
  templateUrl: './add-income-dialog.component.html',
  styleUrls: ['./add-income-dialog.component.scss'],
  standalone: true,
  imports: [
    FormsModule,
    MatInputModule,
    MatCheckboxModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatIconModule,
    CommonModule
  ]
})
export class AddIncomeDialogComponent{
  incomeSource = {
    source: '',
    amount: '',
    date: new Date(),
    categoryId: null as number | null,
    recurring: false,
    description: '',
  };

  categories: Category[] = [];

  dialogTitle: string;
  flag : boolean = false;
  incomeData : any;

  private editCategoryName?: string;

  constructor(
    private httpClient:HttpClient,
    private categoryService: CategoryService,
    private toastr:ToastrService,
    private router: Router,
    public dialogRef: MatDialogRef<AddIncomeDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
  ) {
    const dialogData = data || {};
  
    if (dialogData.isUpdate) {
      this.editCategoryName = dialogData.category; 
      this.dialogTitle = 'Update Income';
      this.incomeSource = { ...dialogData };
      this.flag = true;

       this.incomeData = {
        id:dialogData.id,
        source:dialogData.source,
        date:dialogData.date,
        recurring:dialogData.recurring,
        categoryId:dialogData.categoryId,
        is_deleted:dialogData.is_deleted,
        description:dialogData.description,
      };
      this.loadIncomeCategories();
    } else {
      this.dialogTitle = 'Add New Income';
      this.incomeSource = {
        source: '',
        amount: '',
        date: new Date(),
        categoryId: null,
        recurring: false,
        description: '',
      };
      this.loadIncomeCategories();
    }
  }

  baseUrl = environment.BASE_URL;
  today : Date = new Date();

  isValid(): boolean {
    return (
      this.incomeSource.source.trim() !== '' &&
      this.incomeSource.amount !== '' &&
      this.incomeSource.date !== null &&
      this.incomeSource.categoryId !== null
    );
  }

  loadIncomeCategories(): void {
    this.categoryService.getIncomeCategories()
      .subscribe({
        next: (categories) => {
          this.categories = categories;

          if (this.flag && this.editCategoryName) {
            const matched = this.categories.find(
              c => c.category === this.editCategoryName
            );
            if (matched) {
              this.incomeSource.categoryId = matched.categoryId;
            }
          }
        },
        error: () => {
          this.toastr.error('Failed to load income categories');
        }
      });
  }

  capitalizeFirstLetter() {
    if (this.incomeSource.source && this.incomeSource.source.length > 0) {
      this.incomeSource.source = this.incomeSource.source.charAt(0).toUpperCase() + this.incomeSource.source.slice(1);
    }
  }
  

  onSave() {
    if (this.isValid()) {
      
      if(this.flag == false){
        this.dialogRef.close(this.incomeSource);
      }
      else {
        const incomeDataUpdated = {
          ...this.incomeData,
          amount:this.incomeSource.amount,
          date: this.formatDate(this.incomeData.date),
        };

        this.httpClient.post<IncomeSource[]>(`${this.baseUrl}/api/v1/transaction/income/incomeUpdateCheck`, incomeDataUpdated).subscribe({
          next: (result) => {
            if (result) {
              this.dialogRef.close(this.incomeSource);
            } else {
              this.toastr.warning("Income can't be reduced too low due to expenses");
            }
          },
          error: (error) => {
            console.error('Failed to load income data:', error);
          }
        });
      }

    } else {
      alert('Please fill in all required fields before saving.');
    }
  }


  onCancel() {
    this.dialogRef.close();
  }

  formatDate(date: string | Date): string {
    const d = new Date(date);
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, '0'); // Months are zero-based
    const dd = String(d.getDate()).padStart(2, '0');
    const hh = String(d.getHours()).padStart(2, '0');
    const min = String(d.getMinutes()).padStart(2, '0');
    const ss = String(d.getSeconds()).padStart(2, '0');

    return `${yyyy}-${mm}-${dd}T${hh}:${min}:${ss}`;
  }
}
