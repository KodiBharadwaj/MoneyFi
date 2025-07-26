import { Component } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { NgChartsModule } from 'ng2-charts';
import { ChartConfiguration, ChartData } from 'chart.js';

@Component({
  selector: 'app-home',
  standalone: true,
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
  imports: [CommonModule, NgChartsModule, RouterModule]
})
export class HomeComponent {
  isLoggedIn: boolean = false;

  constructor(private router: Router) {}

    // Polar Area Chart Configuration
    public polarChartData: ChartData<'polarArea'> = {
      labels: ['Bills', 'Food', 'Transport', 'Shopping', 'Entertainment'],
      datasets: [{
        data: [11, 16, 7, 3, 14],
        backgroundColor: [
          'rgba(255, 99, 132, 0.7)',
          'rgba(75, 192, 192, 0.7)',
          'rgba(255, 205, 86, 0.7)',
          'rgba(150, 153, 157, 0.7)',
          'rgba(54, 162, 235, 0.7)',
        ],
      }]
    };
  
    public polarChartOptions: ChartConfiguration['options'] = {
      responsive: true,
      plugins: {
        legend: {
          position: 'right',
        },
        title: {
          display: true,
          text: 'Spending Analysis',
          font: {
            size: 24,
            family: 'Roboto',
          },
          color: '#1e3c72',
        }
      },
      scales: {
        r: {
          ticks: {
            display: false,
          },
          angleLines: {
            display: true,
          },
          grid: {
            circular: true,
          }
        }
      },
      backgroundColor: 'transparent'
    };
    
  navigateTo(route: string): void {
    this.router.navigate([route]);
  }

  isRouteActive(route: string): boolean {
    return this.router.url === route;
  }

  scrollToFeatures() {
    document.getElementById('features')?.scrollIntoView({ behavior: 'smooth' });
  }

}
