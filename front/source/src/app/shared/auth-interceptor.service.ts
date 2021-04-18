import {Injectable} from '@angular/core';
import {HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/operator/do';
import {SecurityService} from './security.service';
import {Router} from "@angular/router";

@Injectable()
export class AuthInterceptorService implements HttpInterceptor {

    constructor(private securityService: SecurityService,
                private router: Router) {

    }

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        request = request.clone({
            setHeaders: {
                Authorization: `BEARER ` + this.securityService.getCurrentToken()
            }
        });
        return next.handle(request)
            .do(e => {
            }, (err: any) => {
                if (err instanceof HttpErrorResponse) {
                    if (err.status === 401) {
                                this.router.navigate(['/login']);
                    }
                }

            });
    }
}
