import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { APP_INITIALIZER, ApplicationConfig, importProvidersFrom } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { authInterceptor } from './auth.interceptor';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { ToastrModule } from 'ngx-toastr';
import { provideNativeDateAdapter } from '@angular/material/core';
import { RuntimeEnvService } from './core/config/runtime-env.service';

export function initEnv(envService: RuntimeEnvService) {
  return () => envService.load();
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])), provideAnimationsAsync(), importProvidersFrom(ToastrModule.forRoot()),
    provideNativeDateAdapter(), provideAnimationsAsync(),
    provideHttpClient(),
    RuntimeEnvService,
    {
      provide: APP_INITIALIZER,
      useFactory: initEnv,
      deps: [RuntimeEnvService],
      multi: true
    }
  ],
};
