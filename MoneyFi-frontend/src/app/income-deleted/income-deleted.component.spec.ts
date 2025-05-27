import { ComponentFixture, TestBed } from '@angular/core/testing';

import { IncomeDeletedComponent } from './income-deleted.component';

describe('IncomeDeletedComponent', () => {
  let component: IncomeDeletedComponent;
  let fixture: ComponentFixture<IncomeDeletedComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [IncomeDeletedComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(IncomeDeletedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
