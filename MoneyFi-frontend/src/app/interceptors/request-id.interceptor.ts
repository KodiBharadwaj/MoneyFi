import { HttpInterceptorFn } from "@angular/common/http";
import { generateRequestId } from "../constants/app-constants";

export const requestIdInterceptor: HttpInterceptorFn = (req, next) => {
  const requestId = generateRequestId();

  const clonedRequest = req.clone({
    headers: req.headers.set('Request-Id', requestId),
  });

  return next(clonedRequest);
};