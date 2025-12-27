import { NgModule } from '@angular/core';
import { NgChartsModule } from 'ng2-charts';
import { Chart } from 'chart.js';
import { registerables } from 'chart.js';

@NgModule({
  declarations: [],
  imports: [
    NgChartsModule,
  ]
})
export class AppModule {
  constructor() {
    Chart.register(...registerables);
  }
}