import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {WorkbookViewComponent} from './workbook/workbook-view/workbook-view.component';
import {EventViewComponent} from './event/event-view/event-view.component';
import {LoginComponent} from './auth/login/login.component';
import {SignupComponent} from './auth/signup/signup.component';
import {VoucherHandleViewComponent} from './auth/voucher-handle-view/voucher-handle-view.component';
import {SecurityGuard} from './shared/security.guard';
import {RegisteredComponent} from './auth/registered/registered.component';
import {UserStatisticsComponent} from "./user-statistics/user-statistics.component";
import {WorkbookResultComponent} from "./workbook-result/workbook-result.component";
import {ProfileComponent} from "./profile/profile.component";
import {ResetPasswordComponent} from "./auth/reset-password/reset-password.component";
import {AccountResettingComponent} from "./auth/account-resetting/account-resetting.component";
import {PersonalInfoDisclaimerComponent} from "./personal-info-disclaimer/personal-info-disclaimer.component";
import {EventSelectComponent} from './event-select/event-select.component';
import {EnterEmailComponent} from './auth/enter-email/enter-email.component';
import {AllWorkbooksComponent} from './all-workbooks/all-workbooks.component';
import {ResendLetterComponent} from './auth/resend-letter/resend-letter.component';
import { ResendingComponent } from './auth/resending/resending.component';

const appRoutes: Routes = [
    {
        path: '',
        pathMatch: 'full',
        redirectTo: 'events',
    },
    {
        path: 'events',
        pathMatch: 'full',
        component: EventSelectComponent,
        canActivate: [SecurityGuard]
    },
    {
        path: 'disclaimer',
        component: PersonalInfoDisclaimerComponent
    },
    {
        path: 'events/:permalink',
        component: EventViewComponent,
        canActivate: [SecurityGuard]
    },
    {
        path: 'workbooks/:id',
        component: WorkbookViewComponent,
        canActivate: [SecurityGuard]
    },
    {
        path: 'workbooks/:id/result',
        component: WorkbookResultComponent,
        canActivate: [SecurityGuard]
    },
    {
        path: 'workbooks',
        component: AllWorkbooksComponent,
        canActivate: [SecurityGuard]
    },
    {
        path: 'statistics',
        component: UserStatisticsComponent,
        canActivate: [SecurityGuard]
    },
    {
        path: 'profile',
        component: ProfileComponent,
        canActivate: [SecurityGuard]
    },
    {
        path: 'login',
        component: LoginComponent
    },
    {
        path: 'registered',
        component: RegisteredComponent
    },
    {
        path: 'signup',
        component: SignupComponent
    },
    {
        path: 'use-security-voucher',
        component: VoucherHandleViewComponent
    },
    {
        path: 'reset-password',
        component: ResetPasswordComponent
    },
    {
        path: 'account-resetting',
        component: AccountResettingComponent
    },
    {
        path: 'enter-email',
        component: EnterEmailComponent,
        canActivate: [SecurityGuard]
    },
    {
        path: 'resend',
        component: ResendLetterComponent
    },
    {
        path: 'resending',
        component: ResendingComponent
    },
    {
        path: '**',
        redirectTo: 'events/joker2017',
    },

];

@NgModule({
    imports: [
        RouterModule.forRoot(appRoutes, {useHash: false})
    ],
    exports: [
        RouterModule
    ]
})
export class AppRoutingModule {
}
