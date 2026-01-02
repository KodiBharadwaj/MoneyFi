import { Injectable } from "@angular/core";

@Injectable({ providedIn: 'root' })
export class SessionGuardService {
  sessionExpiredHandled = false;
}
