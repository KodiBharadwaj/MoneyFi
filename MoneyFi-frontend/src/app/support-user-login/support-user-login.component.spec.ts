import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SupportUserLoginComponent } from './support-user-login.component';

describe('SupportUserLoginComponent', () => {
  let component: SupportUserLoginComponent;
  let fixture: ComponentFixture<SupportUserLoginComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SupportUserLoginComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SupportUserLoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
