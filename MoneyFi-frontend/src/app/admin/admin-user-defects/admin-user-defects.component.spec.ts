import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminUserDefectsComponent } from './admin-user-defects.component';

describe('AdminUserDefectsComponent', () => {
  let component: AdminUserDefectsComponent;
  let fixture: ComponentFixture<AdminUserDefectsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminUserDefectsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminUserDefectsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
