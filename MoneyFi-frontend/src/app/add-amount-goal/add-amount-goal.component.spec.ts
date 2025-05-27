import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddAmountGoalComponent } from './add-amount-goal.component';

describe('AddAmountGoalComponent', () => {
  let component: AddAmountGoalComponent;
  let fixture: ComponentFixture<AddAmountGoalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddAmountGoalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddAmountGoalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
