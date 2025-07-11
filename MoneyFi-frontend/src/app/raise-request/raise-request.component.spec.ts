import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RaiseRequestComponent } from './raise-request.component';

describe('RaiseRequestComponent', () => {
  let component: RaiseRequestComponent;
  let fixture: ComponentFixture<RaiseRequestComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RaiseRequestComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RaiseRequestComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
