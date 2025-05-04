import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { FormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { AddIncomeDialogComponent } from '../add-income-dialog/add-income-dialog.component';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { ChartConfiguration, ChartData, TimeScale } from 'chart.js';
import { NgChartsModule } from 'ng2-charts';
import { MatSelectModule } from '@angular/material/select';
import { CountUpDirective } from '../shared/directives/count-up.directive';
import { ConfirmDeleteDialogComponent } from '../confirm-delete-dialog/confirm-delete-dialog.component';


interface IncomeSource {
  id: number;
  source: string;
  amount: number;
  date: string;
  category: string;
  recurring: boolean;
  is_deleted: boolean;
}

@Component({
  selector: 'app-income',
  templateUrl: './income.component.html',
  styleUrls: ['./income.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    FormsModule,
    MatInputModule,
    AddIncomeDialogComponent,
    NgChartsModule,
    MatSelectModule,
    CountUpDirective
  ]
})
export class IncomeComponent {
  totalIncome: number = 0;
  incomeSources: IncomeSource[] = [];
  deletedIncomeSources: IncomeSource[] = [];
  loading: boolean = false;
  recurringPercentage: number = 0;
  selectedYear: number = new Date().getFullYear();
  selectedMonth: number = 0; // 0 means all months
  selectedCategory: string = '';
  categories: string[] = [
    'Salary', 'Investments', 'Freelance', 'Business', 'Other'
  ];
  months: string[] = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ];
  
  

  availableYears: number[] = [];
  uniqueCategories: string[] = [];

  public pieChartData: ChartData<'pie', number[], string> = {
    labels: [],
    datasets: [{
      data: [],
      backgroundColor: [
        '#FF6384',
        '#36A2EB',
        '#FFCE56',
        '#4BC0C0',
        '#9966FF',
        '#FF9F40'
      ]
    }]
  };

  public pieChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    plugins: {
      legend: {
        display: true,
        position: 'right',
      }
    }
  };

  constructor(public httpClient: HttpClient,private dialog: MatDialog, private router:Router, private toastr:ToastrService) {};

  baseUrl = "http://localhost:8765";
  
  ngOnInit() {
    this.initializeFilters();
    
    // Set the default month to the current month (1-based index)
    this.selectedMonth = new Date().getMonth() + 1; // Current month in 1-based index
    this.selectedYear = new Date().getFullYear(); // Current year
  
    this.loadIncomeData();
  }

  initializeFilters() {
    // Generate last 5 years
    const currentYear = new Date().getFullYear();
    this.availableYears = Array.from({length: 5}, (_, i) => currentYear - i);
  }

  deleted : boolean = false;
  isDeletedClicked(){
    // console.log("clicked");
    this.deleted = true;
    this.loadDeletedIncomeData()
  }

  loadIncomeData() {
    this.loading = true;

    let url: string;
    if(this.selectedCategory === '') this.selectedCategory = "all";
    if (this.selectedMonth === 0) {
      // Fetch all expenses for the selected year
      url = `${this.baseUrl}/api/v1/income/getIncomeDetails/${this.selectedYear}/${this.selectedCategory}/${this.deleted}`;
    } else {
      // Fetch expenses for the specific month and year
      url = `${this.baseUrl}/api/v1/income/getIncomeDetails/${this.selectedMonth}/${this.selectedYear}/${this.selectedCategory}/${this.deleted}`;
    }

    this.httpClient.get<IncomeSource[]>(url).subscribe({
      next: (data) => {
        if (data && data.length > 0) {
          this.incomeSources = data;
          this.calculateTotalIncome();
          this.updateChartData();
        } else {
          this.incomeSources = [];
          this.calculateTotalIncome();
          this.toastr.warning('No income data available. Try adding income', 'No Data');
        }
        this.loading = false;
      },
      error: (error) => {
        console.error('Failed to load income data:', error);
        if(error.status === 401){
          if (error.error === 'TokenExpired'|| 'Token is blacklisted') {
            alert('Your session has expired. Please login again.');
            sessionStorage.removeItem('moneyfi.auth');
            this.router.navigate(['/']); // or your login/landing route
          } else if (error.error === 'InvalidToken') {
            alert('Authorization failed! Please login again.');
            sessionStorage.removeItem('moneyfi.auth');
            this.router.navigate(['/']); // or your login/landing route
          }
          else {
            alert('Service Unavailable!! Please try later');
          }
        }
      },
      complete: () => {
        this.loading = false;
      }
    });
  }

  loadDeletedIncomeData() {
    this.loading = true;

    if(this.selectedCategory === '') this.selectedCategory = "all";
    let url: string;
    if (this.selectedMonth === 0) {
      // Fetch all expenses for the selected year
      url = `${this.baseUrl}/api/v1/income/getIncomeDetails/${this.selectedYear}/${this.selectedCategory}/${this.deleted}`;
    } else {
      // Fetch expenses for the specific month and year
      url = `${this.baseUrl}/api/v1/income/getIncomeDetails/${this.selectedMonth}/${this.selectedYear}/${this.selectedCategory}/${this.deleted}`;
    }

    this.httpClient.get<IncomeSource[]>(url).subscribe({
      next: (data) => {
        if (data && data.length > 0) {
          this.deletedIncomeSources = data;
          this.calculateTotalIncome();
          this.updateChartData();
        } else {
          this.deletedIncomeSources = [];
          this.calculateTotalIncome();
          this.toastr.warning('No deleted income data available in this range', 'No Data');
        }
        this.loading = false;
      },
      error: (error) => {
        console.error('Failed to load income data:', error);
        if(error.status === 401){
          if (error.error === 'TokenExpired'|| 'Token is blacklisted') {
            alert('Your session has expired. Please login again.');
            sessionStorage.removeItem('moneyfi.auth');
            this.router.navigate(['/']); // or your login/landing route
          } else if (error.error === 'InvalidToken') {
            alert('Authorization failed! Please login again.');
            sessionStorage.removeItem('moneyfi.auth');
            this.router.navigate(['/']); // or your login/landing route
          }
          else {
            alert('Service Unavailable!! Please try later');
          }
        }
      },
      complete: () => {
        this.loading = false;
      }
    });
  }


  addIncome() {
    const dialogRef = this.dialog.open(AddIncomeDialogComponent, {
      width: '500px',
      panelClass: 'income-dialog',
    });
  
    dialogRef.afterClosed().subscribe((result) => {

      const formattedDate = this.formatDate(result.date);
      const incomeData = {
        ...result, // This should contain fields like source, amount, date, category, recurring, etc.
        date:formattedDate,
      };

      this.httpClient.post<IncomeSource>(`${this.baseUrl}/api/v1/income/saveIncome`, incomeData).subscribe({
        next: (newIncome) => {
          if(newIncome != null){
            this.incomeSources.push(newIncome);
            this.calculateTotalIncome();
            this.updateChartData();
            this.resetFilters();
            this.toastr.success("Income " + newIncome.source + " added succesfully");
          }
          else{
            this.toastr.warning("Same Income category and source can't be added");
          }
        },
        error: (error) => {
          console.error('Failed to add income:', error);
          if(error.status === 401){
            if (error.error === 'TokenExpired'|| 'Token is blacklisted') {
              alert('Your session has expired. Please login again.');
              sessionStorage.removeItem('moneyfi.auth');
              this.router.navigate(['/']); // or your login/landing route
            } else if (error.error === 'InvalidToken') {
              alert('Authorization failed! Please login again.');
              sessionStorage.removeItem('moneyfi.auth');
              this.router.navigate(['/']); // or your login/landing route
            }
            else {
              alert('Service Unavailable!! Please try later');
            }
          }
        },
        complete: () => {
          this.loading = false;
        }
      });
    });
  }
  

  calculateTotalIncome() {
    this.totalIncome = this.incomeSources.reduce((sum, source) => sum + source.amount, 0);
  }


  updateIncome(income: IncomeSource) {
    const dialogRef = this.dialog.open(AddIncomeDialogComponent, {
      width: '500px',
      panelClass: 'income-dialog',
      data: { ...income, isUpdate:true }, // Pass the income data to the dialog
    });

    dialogRef.afterClosed().subscribe((result) => {
      const formattedDate = this.formatDate(result.date);
      const updatedIncomeData = {
        ...result, // Updated fields from the dialog form
        date: formattedDate,
      };

      this.httpClient.put<IncomeSource>(`${this.baseUrl}/api/v1/income/${income.id}`, updatedIncomeData).subscribe({
        next: (updatedIncome) => {
          if(updatedIncome){
            this.toastr.success("Income of " + updatedIncome.source + " updated successfully");
            this.loadIncomeData();
          } else {
            this.toastr.warning("No changes to update");
          }
        },
        error: (error) => {
          if(error.Status === 204){
            this.toastr.warning("No changes to update");
          }
          console.error('Failed to update income:', error);
          if(error.status === 401){
            if (error.error === 'TokenExpired'|| 'Token is blacklisted') {
              alert('Your session has expired. Please login again.');
              sessionStorage.removeItem('moneyfi.auth');
              this.router.navigate(['/']); // or your login/landing route
            } else if (error.error === 'InvalidToken') {
              alert('Authorization failed! Please login again.');
              sessionStorage.removeItem('moneyfi.auth');
              this.router.navigate(['/']); // or your login/landing route
            }
            else {
              alert('Service Unavailable!! Please try later');
            }
          }
        },
      });
    });
  }

  formatDate(date: string): string {
    const d = new Date(date);
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, '0'); // Months are zero-based
    const dd = String(d.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
  }
  

  deleteIncome(incomeId: number): void {
    const incomedataFetch = this.incomeSources.find(i => i.id === incomeId);
    const dialogRef = this.dialog.open(ConfirmDeleteDialogComponent, {
      width: '400px',
      panelClass: 'custom-dialog-container',
      data: {...incomedataFetch, isIncome:true},
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        const incomeSource = this.incomeSources.find(i => i.id === incomeId);

        this.httpClient.post<IncomeSource[]>(`${this.baseUrl}/api/v1/income/incomeDeleteCheck`, incomeSource).subscribe({
          next: (result) => {
            if (result) {
            
              const index = this.incomeSources.findIndex(i=>i.id === incomeId);
              if (index !== -1) {
                this.incomeSources.splice(index, 1); // Remove the item at the found index
              }
              this.calculateTotalIncome();
              this.updateChartData();
              this.httpClient.delete<void>(`${this.baseUrl}/api/v1/income/${incomeId}`)
                .subscribe({
                  next: () => {
                    this.toastr.warning("Income " +incomeSource?.source+ " has been deleted");
                  },
                  error: (err) => {
                    console.error('Error deleting income:', err);
                  }
                });
              
            } else {
              this.toastr.warning("Income can't be deleted");
            }
            this.loading = false;
          },
          error: (error) => {
            console.error('Failed to delete income:', error);
            if(error.status === 401){
              if (error.error === 'TokenExpired'|| 'Token is blacklisted') {
                alert('Your session has expired. Please login again.');
                sessionStorage.removeItem('moneyfi.auth');
                this.router.navigate(['/']); // or your login/landing route
              } else if (error.error === 'InvalidToken') {
                alert('Authorization failed! Please login again.');
                sessionStorage.removeItem('moneyfi.auth');
                this.router.navigate(['/']); // or your login/landing route
              }
              else {
                alert('Service Unavailable!! Please try later');
              }
            }
          }
        });

      }
    });
  }


  private updateChartData() {
    // Group income sources by category and sum their amounts
    const categoryMap = new Map<string, number>();
    
    this.incomeSources.forEach(income => {
      const currentAmount = categoryMap.get(income.category) || 0;
      categoryMap.set(income.category, currentAmount + income.amount);
    });

    // Update chart data
    this.pieChartData = {
      labels: Array.from(categoryMap.keys()),
      datasets: [{
        data: Array.from(categoryMap.values()),
        backgroundColor: [
          '#FF6384',
          '#36A2EB',
          '#FFCE56',
          '#4BC0C0',
          '#9966FF',
          '#FF9F40'
        ]
      }]
    };

    // Calculate recurring vs one-time ratio
    const recurringTotal = this.incomeSources
      .filter(income => income.recurring)
      .reduce((sum, income) => sum + income.amount, 0);
    
    this.recurringPercentage = this.totalIncome > 0 
      ? Math.round((recurringTotal / this.totalIncome) * 100)
      : 0;
  }


  filterIncome() {
    this.loadIncomeData();
  }

  resetFilters() {
    const today = new Date();
    this.selectedYear = today.getFullYear(); // Reset to the current year
    this.selectedMonth = today.getMonth() + 1; // Reset to the current month (1-based index)
    this.selectedCategory = ''; // Reset to all categories
    this.filterIncome();
  }


  generateReport() {

    let url: string;
    if (this.selectedMonth === 0) {
      url = `${this.baseUrl}/api/v1/income/${this.selectedYear}/${this.selectedCategory}/generateYearlyReport`;
    } else {
      url = `${this.baseUrl}/api/v1/income/${this.selectedMonth}/${this.selectedYear}/${this.selectedCategory}/generateMonthlyReport`;
    }

    this.httpClient.get(url, { responseType: 'blob' }).subscribe({
      next: (response) => {
        // Trigger File Download
        const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Monthly_Report_04_2025.xlsx`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
      },
      error: (error) => {
        console.error('Failed to generate report:', error);
        alert("Failed to generate the report. Please try again.");

        if(error.status === 401){
          if (error.error === 'TokenExpired'|| 'Token is blacklisted') {
            alert('Your session has expired. Please login again.');
            sessionStorage.removeItem('moneyfi.auth');
            this.router.navigate(['/']); // or your login/landing route
          } else if (error.error === 'InvalidToken') {
            alert('Authorization failed! Please login again.');
            sessionStorage.removeItem('moneyfi.auth');
            this.router.navigate(['/']); // or your login/landing route
          }
          else {
            alert('Service Unavailable!! Please try later');
          }
        }
      }
    });
  }
  
}