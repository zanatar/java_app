import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, NgForm, Validators} from '@angular/forms';
import {SignupValidatorsProvider} from '../signup/signup-validators.provider';
import {SecurityService} from '../../shared/security.service';
import {Router} from '@angular/router';
import {UserService} from '../../profile/user.service';

@Component({
    selector: 'app-enter-email',
    templateUrl: './enter-email.component.html',
    styleUrls: ['./enter-email.component.scss']
})
export class EnterEmailComponent implements OnInit {
    emailFormCtrl: FormGroup;

    constructor(private formBuilder: FormBuilder,
                private securityService: SecurityService,
                private userService: UserService,
                private router: Router) {
    }

    ngOnInit() {
        this.emailFormCtrl = this.formBuilder.group({
            email: new FormControl('', {
                validators: [SignupValidatorsProvider.email, Validators.required],
                asyncValidators: SignupValidatorsProvider.emailNotTaken(this.securityService),
                updateOn: 'blur'
            })
        });
    }

    get email() {
        return this.emailFormCtrl.get('email');
    }

    onEnteredEmail(form: NgForm): void {
        const email = form.value.email;
        this.userService.setEmail(email).subscribe(() => {
            this.router.navigate(['/']);
        });
    }

}
