import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminExcelTemplatesComponent } from './admin-excel-templates.component';

describe('AdminExcelTemplatesComponent', () => {
  let component: AdminExcelTemplatesComponent;
  let fixture: ComponentFixture<AdminExcelTemplatesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminExcelTemplatesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminExcelTemplatesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
