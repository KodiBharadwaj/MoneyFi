// ... other imports
import { NgModule } from '@angular/core';
import { NgChartsModule } from 'ng2-charts';
import { Chart } from 'chart.js';
import { registerables } from 'chart.js';

@NgModule({
  declarations: [
    // ... other components
  ],
  // ... rest of the module configuration
  imports: [
    NgChartsModule,
    // ... other imports
  ]
})
export class AppModule {
  constructor() {
    Chart.register(...registerables);
  }
}