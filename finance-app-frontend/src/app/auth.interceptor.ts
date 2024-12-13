import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // alert("Intercepted")
  if (req.url.includes('/register')) {
    return next(req); // Proceed without modifying the request
  }

  const jwt = sessionStorage.getItem('finance.auth')
  if(jwt){
    req = req.clone(
     {
      setHeaders : {Authorization: "Bearer "+jwt}
      }
    
  )
}
  return next(req);
};
