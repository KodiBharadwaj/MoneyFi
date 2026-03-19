import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminUserFeedbackComponent } from './admin-user-feedback.component';

describe('AdminUserFeedbackComponent', () => {
  let component: AdminUserFeedbackComponent;
  let fixture: ComponentFixture<AdminUserFeedbackComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminUserFeedbackComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminUserFeedbackComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
