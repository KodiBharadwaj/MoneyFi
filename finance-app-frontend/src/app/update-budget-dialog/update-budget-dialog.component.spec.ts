import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UpdateBudgetDialogComponent } from './update-budget-dialog.component';

describe('UpdateBudgetDialogComponent', () => {
  let component: UpdateBudgetDialogComponent;
  let fixture: ComponentFixture<UpdateBudgetDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UpdateBudgetDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UpdateBudgetDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
