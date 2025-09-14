import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminReasonsComponent } from './admin-reasons.component';

describe('AdminReasonsComponent', () => {
  let component: AdminReasonsComponent;
  let fixture: ComponentFixture<AdminReasonsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminReasonsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminReasonsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
