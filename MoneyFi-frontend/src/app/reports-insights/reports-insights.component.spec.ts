import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReportsInsightsComponent } from './reports-insights.component';

describe('ReportsInsightsComponent', () => {
  let component: ReportsInsightsComponent;
  let fixture: ComponentFixture<ReportsInsightsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReportsInsightsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReportsInsightsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
