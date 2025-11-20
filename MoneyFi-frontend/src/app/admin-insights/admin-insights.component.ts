import { HttpClient } from '@angular/common/http';
import { Component, OnInit, ViewChild } from '@angular/core';
import { ActiveElement, ChartConfiguration, ChartEvent } from 'chart.js';
import { BaseChartDirective, NgChartsModule } from 'ng2-charts';
import { environment } from '../../environments/environment';
import { ToastrService } from 'ngx-toastr';
import { Router, RouterModule } from '@angular/router';
import { ConfirmLogoutDialogComponent } from '../confirm-logout-dialog/confirm-logout-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-admin-insights',
  standalone: true,
  imports: [NgChartsModule, RouterModule, FormsModule, CommonModule],
  templateUrl: './admin-insights.component.html',
  styleUrl: './admin-insights.component.css'
})
export class AdminInsightsComponent implements OnInit{

  constructor(private http: HttpClient, private router:Router, private dialog: MatDialog, private toastr:ToastrService) {}
  baseUrl = environment.BASE_URL;

  selectedYear: number = new Date().getFullYear();
  years: number[] = []; // will hold last 10 years

  ngOnInit(): void {
    this.initYears();
    this.loadUserMonthlyData();
    this.loadUserCountChart();
  }

  initYears(): void {
    const currentYear = new Date().getFullYear();
    this.years = [];           // ← re-initialize just to be safe
    for (let i = 0; i < 10; i++) {
      this.years.push(currentYear - i);
    }
  }


userMonthlyChartData: ChartConfiguration<'bar'>['data'] = {
    labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
             'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
    datasets: [
      {
        label: 'User Signups',
        data: Array(12).fill(0),
        backgroundColor: 'rgba(54, 162, 235, 0.6)',
        borderColor: 'rgba(54, 162, 235, 1)',
        borderWidth: 1
      }
    ]
  };

  userMonthlyChartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    scales: {
      y: {
        beginAtZero: true,
        ticks: {
          stepSize: 1
        },
        title: {
          display: true,
          text: 'User Count'
        }
      },
      x: {
        title: {
          display: true,
          text: 'Month'
        }
      }
    },
    plugins: {
      title: {
        display: true,
        text: 'Monthly User Creation Count',
        font: {
          size: 18
        }
      }
    }
  };

  loadUserMonthlyData(): void {
    const status = 'All'; // or any status you want to filter
    this.http.get<{ [key: number]: number }>(`${this.baseUrl}/api/v1/user-service/admin/${this.selectedYear}/user-monthly-count/chart?status=${status}`)
      .subscribe(data => {
        const monthlyCounts = Array(12).fill(0);
        for (let month = 1; month <= 12; month++) {
          monthlyCounts[month - 1] = data[month] || 0;
        }
        // this.userMonthlyChartData.datasets[0].data = monthlyCounts;
        this.userMonthlyChartData = {
        ...this.userMonthlyChartData,
        datasets: [
          {
            ...this.userMonthlyChartData.datasets[0],
            data: monthlyCounts
          }
        ]
      };

      }, error => {
        console.error('Error loading chart data', error);
      });
  }

  dummyChartData: ChartConfiguration<'bar'>['data'] = {
  labels: ['Active Users', 'Blocked Users', 'Deleted Users'],
  datasets: [
    {
      label: 'User Status Distribution',
      data: [0, 0, 0],
      backgroundColor: [
        'rgba(54, 162, 235, 0.7)',   // Active
        'rgba(255, 99, 132, 0.7)',   // Blocked
        'rgba(255, 206, 86, 0.7)'    // Deleted
      ],
      borderColor: [
        'rgba(54, 162, 235, 1)',
        'rgba(255, 99, 132, 1)',
        'rgba(255, 206, 86, 1)'
      ],
      borderWidth: 1
    }
  ]
};

handleChartClick(event: { event?: ChartEvent, active?: {}[] | undefined }) {
  const activeElements = event.active as ActiveElement[] | undefined;
  this.onChartClick(event.event, activeElements);
}

onChartClick(event: ChartEvent | undefined, activeElements: ActiveElement[] | undefined) {
  if (activeElements && activeElements.length > 0) {
    const chartElement = activeElements[0];
    const datasetIndex = chartElement.datasetIndex;
    const index = chartElement.index;

    const label = this.dummyChartData.labels?.[index];

    if (label === 'Active Users') {
      this.router.navigate(['/admin/users', 'ACTIVE']);
    } else if (label === 'Blocked Users') {
      this.router.navigate(['/admin/users', 'BLOCKED']);
    } else if (label === 'Deleted Users') {
      this.router.navigate(['/admin/users', 'DELETED']);
    }
  }
}

dummyChartOptions: ChartConfiguration<'bar'>['options'] = {
  responsive: true,
  interaction: {
    mode: 'nearest', // Or 'index'
    intersect: true
  },
  onClick: (event, elements) => {
    // we'll use a separate method for this
    this.onChartClick(event, elements);
  },
  scales: {
    y: {
      beginAtZero: true,
      title: {
        display: true,
        text: 'Number of Users'
      }
    },
    x: {
      title: {
        display: true,
        text: 'Status'
      }
    }
  },
  plugins: {
    title: {
      display: true,
      text: 'User Status Breakdown',
      font: {
        size: 18
      }
    }
  }
};

@ViewChild(BaseChartDirective) chart?: BaseChartDirective;


  loadUserCountChart(): void {
    this.http.get<any>(`${this.baseUrl}/api/v1/user-service/admin/overview-user-details`)
    .subscribe(data => {
      const { activeUsers, blockedUsers, deletedUsers } = data;

      const userCounts = [
        activeUsers || 0,
        blockedUsers || 0,
        deletedUsers || 0
      ];

      this.dummyChartData = {
        ...this.dummyChartData,
        datasets: [{
          ...this.dummyChartData.datasets[0],
          data: userCounts
        }]
      };

      this.chart?.update(); // force refresh
    }, error => {
      console.error('Error loading chart data', error);
    });
  }

  logoutUser(): void {
        const dialogRef = this.dialog.open(ConfirmLogoutDialogComponent, {
          width: '400px',
          panelClass: 'custom-dialog-container',
        });
      
        dialogRef.afterClosed().subscribe((result) => {
          if (result) {
    
            this.http.post(`${this.baseUrl}/api/v1/user-admin/logout`, {}, { responseType: 'text' }).subscribe({
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

  onYearChange(): void {
    this.loadUserMonthlyData();
  }
}