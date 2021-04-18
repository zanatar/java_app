import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {SecurityService} from '../../shared/security.service';
import {FormBuilder, FormControl, FormGroup, NgForm, Validators} from "@angular/forms";
import {SignupValidatorsProvider} from "./signup-validators.provider";


@Component({
    selector: 'app-signup',
    templateUrl: './signup.component.html',
    styleUrls: ['./signup.component.css']
})
export class SignupComponent implements OnInit {
    signUpCtrl: FormGroup;

    constructor(private route: ActivatedRoute,
                private router: Router,
                private securityService: SecurityService,
                private formBuilder: FormBuilder) {
    }

    ngOnInit(): void {
        this.securityService.isAuthenticated().subscribe(auth => {
            if (auth) {
                this.leave();
            }
        });
        this.signUpCtrl = this.formBuilder.group({
                quickname: ['', [Validators.required]],
                email: new FormControl('', {
                    validators: [SignupValidatorsProvider.email, Validators.required],
                    asyncValidators: SignupValidatorsProvider.emailNotTaken(this.securityService),
                    updateOn: 'blur'
                })
            }
        );
    }

    get quickname() {
        return this.signUpCtrl.get('quickname');
    }

    get email() {
        return this.signUpCtrl.get('email')
    }



    onSignup(form: NgForm): void {
        const quickname = form.value.quickname;
        const email = form.value.email;

        this.securityService.signup(quickname, email).subscribe(() => {
            this.router.navigate(['/registered']);
        });
    }

    private leave(): void {
        this.router.navigate(['/']);
    }
}
