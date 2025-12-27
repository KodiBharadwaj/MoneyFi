import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GmailSyncDialogComponent } from './gmail-sync-dialog.component';

describe('GmailSyncDialogComponent', () => {
  let component: GmailSyncDialogComponent;
  let fixture: ComponentFixture<GmailSyncDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GmailSyncDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GmailSyncDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
