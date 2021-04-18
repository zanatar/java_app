import {AfterViewInit, Component, NgZone, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {SecurityService} from "../../shared/security.service";
import {FormBuilder, FormGroup, NgForm, Validators} from "@angular/forms";
import {UserService} from '../../profile/user.service';

declare var gapi;

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit, AfterViewInit {
    loginFormCtrl: FormGroup;

    constructor(private route: ActivatedRoute,
                private router: Router,
                private securityService: SecurityService,
                private userService: UserService,
                private formBuilder: FormBuilder,
                private ngZone: NgZone) {
        window['onSignIn'] = (user) => ngZone.run(() => this.onSignIn(user));
        window['onFailure'] = (error) => ngZone.run(() => this.onFailure(error));
        window['authInfo'] = (response) => ngZone.run(() => this.authInfo(response));
    }

    ngOnInit(): void {
        this.securityService.isAuthenticated().subscribe(auth => {
            if (auth) {
                this.leave();
            }
        });
        this.loginFormCtrl = this.formBuilder.group({
            email: ['', [Validators.required]],
            password: ['', [Validators.required]]
        });
    }

    ngAfterViewInit(){
        gapi.load('auth2', () => this.onloaded());
    }

    get email() {
        return this.loginFormCtrl.get('email')
    }

    get password() {
        return this.loginFormCtrl.get('password');
    }

    onLogin(form: NgForm): void {
        const email = form.value.email;
        const password = form.value.password;
        this.securityService.login(email, password, () => {
            this.leave();
        });
    }

    private leave(): void {
        const firstUrl = this.securityService.firstUrl;
        this.securityService
            .checkAccessToPreviousBook()
            .subscribe((w) => this.router.navigate(['/workbooks/' + w.id]),
                () => this.router.navigate([firstUrl? firstUrl : '/']));
    }

    private vkLeave(): void {
        this.userService
            .checkHasEmail()
            .subscribe((w) => w? this.leave() : this.router.navigate(['enter-email']));
    }

    onloaded(){
        gapi.auth2.init({
            client_id: '836803276159-hsrruuvdftmuorg4h95r78cginlp1860.apps.googleusercontent.com',
            cookiepolicy: 'single_host_origin',
        });
        let element = document.getElementById('googleBtn');
        gapi.auth2.getAuthInstance().attachClickHandler(element, {},
            (u) => this.onSignIn(u), this.onFailure);
    }

    onSignIn(googleUser) {
        this.ngZone.run(() => {
            const idToken = googleUser.getAuthResponse().id_token;
            this.securityService.loginWithGoogle(idToken, () => {
                this.leave();
            });
        }
    );
    }

    onFailure(error) {
        console.log(error);
    }

    authInfo(response) {
        
        this.securityService.loginWithVk(response.session, () => {
            this.vkLeave();
        });
    }
}
