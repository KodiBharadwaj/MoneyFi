import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CustomerCareHomeComponent } from './customer-care-home.component';

describe('CustomerCareHomeComponent', () => {
  let component: CustomerCareHomeComponent;
  let fixture: ComponentFixture<CustomerCareHomeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CustomerCareHomeComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CustomerCareHomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
