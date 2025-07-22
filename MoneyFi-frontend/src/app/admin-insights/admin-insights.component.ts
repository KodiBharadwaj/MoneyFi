import { HttpClient } from '@angular/common/http';
import { Component, OnInit, ViewChild } from '@angular/core';
import { ChartConfiguration } from 'chart.js';
import { BaseChartDirective, NgChartsModule } from 'ng2-charts';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-admin-insights',
  standalone: true,
  imports: [NgChartsModule],
  templateUrl: './admin-insights.component.html',
  styleUrl: './admin-insights.component.css'
})
export class AdminInsightsComponent implements OnInit{
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

  constructor(private http: HttpClient) {}
  baseUrl = environment.BASE_URL;

  ngOnInit(): void {
    this.loadUserMonthlyData();
    this.loadUserCountChart();
  }

  loadUserMonthlyData(): void {
    const currentYear = new Date().getFullYear();
    const status = 'All'; // or any status you want to filter

    this.http.get<{ [key: number]: number }>(`${this.baseUrl}/api/v1/admin/${currentYear}/user-monthly-count/chart?status=${status}`)
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

dummyChartOptions: ChartConfiguration<'bar'>['options'] = {
  responsive: true,
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
    this.http.get<any>(`${this.baseUrl}/api/v1/admin/overview-user-details`)
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
}