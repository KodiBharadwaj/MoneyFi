import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GmailSyncCalendarComponent } from './gmail-sync-calendar.component';

describe('GmailSyncCalendarComponent', () => {
  let component: GmailSyncCalendarComponent;
  let fixture: ComponentFixture<GmailSyncCalendarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GmailSyncCalendarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GmailSyncCalendarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
