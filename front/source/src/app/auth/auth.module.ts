import {NgModule} from '@angular/core';
import {SharedModule} from "../shared/shared.module";
import {LoginComponent} from "./login/login.component";
import {RegisteredComponent} from "./registered/registered.component";
import {SignupComponent} from "./signup/signup.component";
import {ReactiveFormsModule} from "@angular/forms";
import {SignupValidatorsProvider} from "./signup/signup-validators.provider";
import {ResetPasswordComponent} from "./reset-password/reset-password.component";
import {VoucherHandleViewComponent} from "./voucher-handle-view/voucher-handle-view.component";
import {AccountResettingComponent} from "./account-resetting/account-resetting.component";
import { EnterEmailComponent } from './enter-email/enter-email.component';
import { ResendLetterComponent } from './resend-letter/resend-letter.component';
import { ResendingComponent } from './resending/resending.component';

@NgModule({
    imports: [
        SharedModule,
        ReactiveFormsModule
    ],
    declarations: [
        LoginComponent,
        RegisteredComponent,
        SignupComponent,
        ResetPasswordComponent,
        VoucherHandleViewComponent,
        AccountResettingComponent,
        EnterEmailComponent,
        ResendLetterComponent,
        ResendingComponent
    ],
    exports: [],
    providers: [SignupValidatorsProvider]
})
export class AuthModule {

}
