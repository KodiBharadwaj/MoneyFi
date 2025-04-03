import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddExpenseDialogComponent } from './add-expense-dialog.component';

describe('AddExpenseDialogComponent', () => {
  let component: AddExpenseDialogComponent;
  let fixture: ComponentFixture<AddExpenseDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddExpenseDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddExpenseDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
