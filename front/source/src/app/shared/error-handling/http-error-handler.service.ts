import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {ErrorMessageMap} from "./error-message.map";
import {CustomErrorModal} from "./custom-error-modal/custom-error-modal.component";
import {Observable} from "rxjs";
import {Injectable} from "@angular/core";
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from "@angular/common/http";


@Injectable()
export class HttpErrorHandler implements HttpInterceptor {
    constructor(private modalService: NgbModal) {
    }

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        return next.handle(request).do(() => {
        }, resp => {
            if (!request.headers.has('ignoreError')) {
                let description = ErrorMessageMap.getDescription(resp.error);
                let modal = this.modalService.open(CustomErrorModal);
                modal.componentInstance.exception = description;
            }
        });
    }
}