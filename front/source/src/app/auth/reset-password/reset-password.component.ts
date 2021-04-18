import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, NgForm, Validators} from "@angular/forms";
import {SecurityService} from "../../shared/security.service";
import {Router} from "@angular/router";

@Component({
    selector: 'app-reset-password',
    templateUrl: './reset-password.component.html',
    styleUrls: ['./reset-password.component.scss']
})
export class ResetPasswordComponent implements OnInit {

    resetPasswordCtrl: FormGroup;

    constructor(private formBuilder: FormBuilder,
                private securityService: SecurityService,
                private router: Router) {
    }

    ngOnInit() {
        this.securityService.isAuthenticated().subscribe(auth => {
            if (auth) {
                this.leave();
            }
        });
        this.resetPasswordCtrl = this.formBuilder.group({
            email: ['', [Validators.required]]
        })
    }

    get email() {
        return this.resetPasswordCtrl.get('email');
    }

    sendVoucher(form: NgForm) {
        this.securityService.resetAccount(form.value.email, () => this.done(), () => {});
    }

    private leave() {
        this.router.navigate(['/']);
    }

    private done() {
        this.router.navigate(['/account-resetting']);
    }

}
