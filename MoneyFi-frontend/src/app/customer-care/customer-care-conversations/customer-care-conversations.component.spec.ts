import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CustomerCareConversationsComponent } from './customer-care-conversations.component';

describe('CustomerCareConversationsComponent', () => {
  let component: CustomerCareConversationsComponent;
  let fixture: ComponentFixture<CustomerCareConversationsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CustomerCareConversationsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CustomerCareConversationsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
