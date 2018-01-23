import { Injectable } from '@angular/core';
import { Request, XHRBackend, RequestOptions, Response, Http, RequestOptionsArgs, Headers } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/catch';
import 'rxjs/add/observable/throw';
import { Router } from '@angular/router';


@Injectable()
export class AuthenticatedHttpService extends Http {

  constructor(backend: XHRBackend, defaultOptions: RequestOptions, private router: Router) {
    super(backend, defaultOptions);
  }

  addAuthHeaderIfAvailable(options?: RequestOptionsArgs): RequestOptionsArgs {
    if (!options) {
      options = new RequestOptions({});
    }

    let object = localStorage.getItem('CloudRefUser');
    if (object != null) {
      let userInfo = JSON.parse(object);

      let user = userInfo.username;
      let password = userInfo.password;

      if (user != null && password != null) {
        if (!options.headers) {
          options.headers = new Headers();
        }
        options.headers.append('Authorization', 'Basic ' + btoa(user + ':' + password));
      }
    }
    return options;
  }

  request(url: string | Request, options?: RequestOptionsArgs): Observable<Response> {

    return super.request(url, this.addAuthHeaderIfAvailable(options)).catch((error: Response) => {
      if (error.status === 401) {
        // delete user account information
        localStorage.removeItem('CloudRefUser');

        // redirect if not at login page
        if (!window.location.href.toString().endsWith('/login')) {
          // user is not logged in
          this.router.navigate(['/login']);
        }
      }
      return Observable.throw(error);
    });
  }
}
