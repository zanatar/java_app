import {NgModule} from '@angular/core';
import {HttpClientModule} from '@angular/common/http';
import {FormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';

import {AuthInterceptorService} from './auth-interceptor.service';
import {SecurityService} from './security.service';
import {SecurityGuard} from './security.guard';
import {ConfigurationService} from './configuration.service';
import {BrowserModule} from "@angular/platform-browser";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {CustomErrorModal} from "./error-handling/custom-error-modal/custom-error-modal.component";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {QRCodeModule} from "angularx-qrcode";
import {LoadingBarRouterModule} from "@ngx-loading-bar/router";
import {LoadingBarHttpClientModule} from "@ngx-loading-bar/http-client";
import {HttpErrorHandler} from "./error-handling/http-error-handler.service";

@NgModule({
    declarations: [
        CustomErrorModal
    ],
    imports: [
        FormsModule,
        QRCodeModule,
        HttpClientModule,
        RouterModule,
        NgbModule,
        BrowserModule,
        BrowserAnimationsModule,
        LoadingBarRouterModule,
        LoadingBarHttpClientModule
    ],
    exports: [
        FormsModule,
        QRCodeModule,
        HttpClientModule,
        RouterModule,
        BrowserModule,
        BrowserAnimationsModule,
        LoadingBarRouterModule,
        LoadingBarHttpClientModule
    ],
    providers: [
        AuthInterceptorService,
        HttpErrorHandler,
        ConfigurationService,
        SecurityService,
        SecurityGuard
    ],
    entryComponents: [
        CustomErrorModal
    ]
})
export class SharedModule {

}
