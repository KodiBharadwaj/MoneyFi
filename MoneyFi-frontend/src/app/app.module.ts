// ... other imports
import { NgModule } from '@angular/core';
import { NgChartsModule } from 'ng2-charts';
import { Chart } from 'chart.js';
import { registerables } from 'chart.js';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import { BrowserModule } from '@angular/platform-browser';

@NgModule({
  declarations: [
    // ... other components
  ],
  // ... rest of the module configuration
  imports: [
    NgChartsModule,
  ]
})
export class AppModule {
  constructor() {
    Chart.register(...registerables);
  }
}