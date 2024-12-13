import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddIncomeDialogComponent } from './add-income-dialog.component';

describe('AddIncomeDialogComponent', () => {
  let component: AddIncomeDialogComponent;
  let fixture: ComponentFixture<AddIncomeDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddIncomeDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddIncomeDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
