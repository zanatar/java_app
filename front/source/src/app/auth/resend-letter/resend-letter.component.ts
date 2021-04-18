import { Component, OnInit } from '@angular/core';
import {FormBuilder, FormGroup, NgForm, Validators} from "@angular/forms";
import {SecurityService} from "../../shared/security.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-resend-letter',
  templateUrl: './resend-letter.component.html',
  styleUrls: ['./resend-letter.component.scss']
})
export class ResendLetterComponent implements OnInit {

    resendLetterCtrl: FormGroup;

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
        this.resendLetterCtrl = this.formBuilder.group({
            email: ['', [Validators.required]]
        });
    }

    get email() {
        return this.resendLetterCtrl.get('email');
    }

    sendVoucher(form: NgForm) {
        this.securityService.resendLetter(form.value.email, () => this.done(), () => {});
    }

    private leave() {
        this.router.navigate(['/']);
    }

    private done() {
        this.router.navigate(['/resending']);
    }

}
