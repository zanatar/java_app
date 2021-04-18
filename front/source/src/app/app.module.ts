import {BrowserModule} from '@angular/platform-browser';
import {HTTP_INTERCEPTORS} from '@angular/common/http';
import {NgModule} from '@angular/core';

import {AppComponent} from './app.component';
import {SharedModule} from './shared/shared.module';
import {AppRoutingModule} from './app-routing.module';
import {HeaderComponent} from './header/header.component';
import {WorkbookModule} from './workbook/workbook.module';
import {EventModule} from './event/event.module';
import {AuthInterceptorService} from './shared/auth-interceptor.service';
import {AuthModule} from "./auth/auth.module";
import {ReactiveFormsModule} from "@angular/forms";
import {UserStatisticsModule} from "./user-statistics/user-statistics.module";
import {FooterComponent} from './footer/footer.component';
import {ProfileModule} from "./profile/profile.module";
import {PersonalInfoDisclaimerComponent} from './personal-info-disclaimer/personal-info-disclaimer.component';
import {HttpErrorHandler} from "./shared/error-handling/http-error-handler.service";
import {EventSelectComponent} from './event-select/event-select.component';
import {AllWorkbooksModule} from './all-workbooks/all-workbooks.module';
import { Select2Module } from 'ng2-select2';

@NgModule({
    declarations: [
        AppComponent,
        HeaderComponent,
        FooterComponent,
        PersonalInfoDisclaimerComponent,
        EventSelectComponent
    ],
    imports: [
        BrowserModule,
        ReactiveFormsModule,
        AllWorkbooksModule,
        AppRoutingModule,
        AuthModule,
        EventModule,
        WorkbookModule,
        UserStatisticsModule,
        ProfileModule,
        SharedModule,
        Select2Module
    ],
    providers: [
        {
            provide: HTTP_INTERCEPTORS,
            useClass: AuthInterceptorService,
            multi: true
        },
        {
            provide: HTTP_INTERCEPTORS,
            useClass: HttpErrorHandler,
            multi: true
        }
    ],
    bootstrap: [AppComponent]
})
export class AppModule {

}
