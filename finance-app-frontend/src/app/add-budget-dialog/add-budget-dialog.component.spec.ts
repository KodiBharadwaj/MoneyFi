import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddBudgetDialogComponent } from './add-budget-dialog.component';

describe('AddBudgetDialogComponent', () => {
  let component: AddBudgetDialogComponent;
  let fixture: ComponentFixture<AddBudgetDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddBudgetDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddBudgetDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
