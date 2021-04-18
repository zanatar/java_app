import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from '@angular/router';
import {SecurityService} from './security.service';
import {Observable} from "rxjs";

@Injectable()
export class SecurityGuard implements CanActivate {


    constructor(private authService: SecurityService,
                private router: Router) {
    }

    canActivate(route: ActivatedRouteSnapshot,  state: RouterStateSnapshot): Observable<boolean> {
        if (!this.authService.firstUrl) {
            this.authService.saveFirstUrl(state.url);
            console.log('saving first url', this.authService.firstUrl);
        }

        return this.authService.isAuthenticated().map(auth => {
            if (auth) {
                return true;
            } else {
                this.router.navigate(['/login']);
                return false;
            }
        });
    }
}
