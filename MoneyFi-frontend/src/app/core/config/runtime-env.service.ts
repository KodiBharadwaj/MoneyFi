import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class RuntimeEnvService {
  constructor(private http: HttpClient) {}

  load(): Promise<void> {
    return this.http
      .get<any>('/assets/env/runtime-env.json')
      .toPromise()
      .then((config) => {
        Object.assign(environment, config);
      })
      .catch((err) => {
        console.error('Failed to load runtime env', err);
      });
  }
}
