import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GmailSyncSummaryComponent } from './gmail-sync-summary.component';

describe('GmailSyncSummaryComponent', () => {
  let component: GmailSyncSummaryComponent;
  let fixture: ComponentFixture<GmailSyncSummaryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GmailSyncSummaryComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GmailSyncSummaryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
