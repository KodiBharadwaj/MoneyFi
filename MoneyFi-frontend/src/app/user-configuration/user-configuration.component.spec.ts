import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserConfigurationComponent } from './user-configuration.component';

describe('UserConfigurationComponent', () => {
  let component: UserConfigurationComponent;
  let fixture: ComponentFixture<UserConfigurationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserConfigurationComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UserConfigurationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
