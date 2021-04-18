import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {SecurityService} from '../../shared/security.service';
import {FormBuilder, FormGroup, NgForm, Validators} from "@angular/forms";
import {SecurityVoucher} from "../../shared/security.model";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {CustomErrorModal} from "../../shared/error-handling/custom-error-modal/custom-error-modal.component";
import {SignupValidatorsProvider} from "../signup/signup-validators.provider";

@Component({
    selector: 'app-voucher-handle-view',
    templateUrl: './voucher-handle-view.component.html',
    styleUrls: ['./voucher-handle-view.component.css']
})
export class VoucherHandleViewComponent implements OnInit {

    voucherCtrl: FormGroup;
    voucher: SecurityVoucher;

    constructor(private route: ActivatedRoute,
                private router: Router,
                private securityService: SecurityService,
                private modalService: NgbModal,
                private formBuilder: FormBuilder) {

    }


    get password() {
        return this.voucherCtrl.get('password')
    }

    get passwordTwice() {
        return this.voucherCtrl.get('passwordTwice')
    }

    ngOnInit(): void {
        this.securityService.isAuthenticated().subscribe(auth => {
            if (auth) {
                this.leave();
            }
        });
        this.route.queryParams.subscribe((params) => {
            const payload = params['payload'];
            this.securityService.retrieveVoucherByPayload(payload, () => this.leave()).subscribe(v => {
                this.voucher = v;
                if (!v) {
                    let modal = this.modalService.open(CustomErrorModal);
                    modal.componentInstance.exception = 'Ваучер не действителен'
                    modal.result.then(() => this.leave(), () => this.leave());
                }
            })
        });

        this.voucherCtrl = this.formBuilder.group({
            password: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(30)]],
            passwordTwice: ['']
        }, {
            validator: [SignupValidatorsProvider.passwordsAreEqual()]
        })
    }

    verify(form: NgForm) {
        const password = form.value.password;
        const passwordTwice = form.value.passwordTwice;
        if (password !== passwordTwice) {
            let modal = this.modalService.open(CustomErrorModal);
            modal.componentInstance.exception = 'Пароли не совпадают';
            return;
        }

        this.securityService.verifyAccountByVoucher(this.voucher.payload, password,
            () => {
                this.securityService.authenticated.next(true);
                this.leave()
            }, () => this.leave());
    }

    private leave(): void {
        const firstUrl = this.securityService.firstUrl;
        this.securityService
            .checkAccessToPreviousBook()
            .subscribe((w) => this.router.navigate(['/workbooks/' + w.id]),
                () => this.router.navigate([firstUrl? firstUrl : '/']));
    }
}
