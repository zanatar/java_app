import {NgModule} from '@angular/core';

import {SharedModule} from '../shared/shared.module';
import {ProfileComponent} from "./profile.component";
import {ReactiveFormsModule} from "@angular/forms";
import {CommonModule} from "@angular/common";
import {UserService} from "./user.service";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";

@NgModule({
    imports: [
        CommonModule,
        SharedModule,
        ReactiveFormsModule,
        NgbModule
    ],
    declarations: [
        ProfileComponent
    ],
    exports: [],
    providers: [
        UserService
    ]
})
export class ProfileModule {

}
