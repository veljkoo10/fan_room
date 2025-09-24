import { Injectable } from '@angular/core';
import { CanActivate, Router, UrlTree, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  public canActivate(
    route: ActivatedRouteSnapshot
  ): boolean | UrlTree {

    if (!this.authService.isLoggedIn()) {
      return this.router.createUrlTree(['/login']);
    }

    const userRole = this.authService.getUserRole();
    const adminOnly = route.data['adminOnly'] || false;

    if (adminOnly && userRole !== 'ADMIN') {
      return this.router.createUrlTree(['/not-authorized']);
    }

    return true;
  }
}
