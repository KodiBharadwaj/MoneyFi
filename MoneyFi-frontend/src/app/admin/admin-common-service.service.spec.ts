import { TestBed } from '@angular/core/testing';

import { AdminCommonServiceService } from './admin-common-service.service';

describe('AdminCommonServiceService', () => {
  let service: AdminCommonServiceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AdminCommonServiceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
