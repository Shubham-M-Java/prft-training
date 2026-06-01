import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // Add X-Requested-With header to all API requests
  // This prevents the browser from showing the native Basic Auth dialog
  // when the server returns a 401 WWW-Authenticate challenge
  const modifiedReq = req.clone({
    setHeaders: {
      'X-Requested-With': 'XMLHttpRequest'
    }
  });
  return next(modifiedReq);
};
