import { Injectable } from '@angular/core';
import { Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';

@Injectable()
export class AuthGuard implements CanActivate {

  constructor(private router: Router) { }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {

    let object = localStorage.getItem('CloudRefUser');

    // check if user is logged in
    if (object != null) {
      let userInfo = JSON.parse(object);

      let user = userInfo.username;
      let password = userInfo.password;
      let role = userInfo.role;

      // check if login information exists
      if (user != null && password != null && role != null) {
        // user is logged in
        if (state.url === '/login' || state.url === '/register') {
          // redirect to references
          this.router.navigate(['']);
          return false;
        } else {
          // logged in -> return true
          return true;
        }
      }
    }

    // not logged in
    if (state.url === '/login' || state.url === '/register') {
      return true;
    } else {
      // not logged in so redirect to login page
      this.router.navigate(['/login']); // { queryParams: { returnUrl: state.url }});
      return false;
    }
  }

}
