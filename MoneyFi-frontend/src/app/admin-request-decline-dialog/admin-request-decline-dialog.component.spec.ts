import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminRequestDeclineDialogComponent } from './admin-request-decline-dialog.component';

describe('AdminRequestDeclineDialogComponent', () => {
  let component: AdminRequestDeclineDialogComponent;
  let fixture: ComponentFixture<AdminRequestDeclineDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminRequestDeclineDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminRequestDeclineDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
