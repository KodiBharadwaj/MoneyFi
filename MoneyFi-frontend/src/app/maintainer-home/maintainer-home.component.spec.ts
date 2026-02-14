import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MaintainerHomeComponent } from './maintainer-home.component';

describe('MaintainerHomeComponent', () => {
  let component: MaintainerHomeComponent;
  let fixture: ComponentFixture<MaintainerHomeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MaintainerHomeComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MaintainerHomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
