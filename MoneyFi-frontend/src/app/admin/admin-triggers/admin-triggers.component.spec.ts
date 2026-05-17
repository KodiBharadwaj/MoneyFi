import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminTriggersComponent } from './admin-triggers.component';

describe('AdminTriggersComponent', () => {
  let component: AdminTriggersComponent;
  let fixture: ComponentFixture<AdminTriggersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminTriggersComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminTriggersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
