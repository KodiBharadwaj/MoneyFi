import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SignupOtpConfirmDialogComponent } from './signup-otp-confirm-dialog.component';

describe('SignupOtpConfirmDialogComponent', () => {
  let component: SignupOtpConfirmDialogComponent;
  let fixture: ComponentFixture<SignupOtpConfirmDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SignupOtpConfirmDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SignupOtpConfirmDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
