import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminUserSessionTimeComponent } from './admin-user-session-time.component';

describe('AdminUserSessionTimeComponent', () => {
  let component: AdminUserSessionTimeComponent;
  let fixture: ComponentFixture<AdminUserSessionTimeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminUserSessionTimeComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminUserSessionTimeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
