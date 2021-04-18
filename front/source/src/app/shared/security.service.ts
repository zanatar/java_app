import {Injectable, NgZone} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {SecurityToken, SecurityVoucher} from './security.model';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/operator/catch';
import 'rxjs/add/observable/of';
import {ConfigurationService} from './configuration.service';
import {Workbook} from '../workbook/workbook.model';
import {Subject} from "rxjs";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {CustomErrorModal} from "./error-handling/custom-error-modal/custom-error-modal.component";
declare var gapi: any;
@Injectable()
export class SecurityService {

    private storage = localStorage;
    authenticated = new Subject<boolean>();
    private readonly backlinkTemplate = '/use-security-voucher?payload={{payload}}';

    constructor(private http: HttpClient,
                private configurationService: ConfigurationService,
                private modalService: NgbModal,
                private ngZone: NgZone) {

    }

    isAuthenticated(): Observable<boolean> {
        const url = this.configurationService.getApiBaseUrl() + '/security/tokens/validator';
        return this.http.get<boolean>(url, {headers: {ignoreLoadingBar: '', ignoreError: ''}})
            .catch(() => {
                return Observable.of(false);
            }).map(auth => {
                if (!this.authenticated) {
                    this.removeFirstUrl();
                    this.removeTokenPayload();

                }
                this.authenticated.next(auth);
                return auth;
            })
    }

    getCurrentToken(): string {
        return this.loadTokenPayload();
    }

    login(email: string, password: string, done): void {
        const invoice = {
            method: 'PASSWORD',
            email: email,
            password: password
        };
        const url = this.configurationService.getApiBaseUrl() + '/security/tokens/';
        const observable = this.http.post<SecurityToken>(url, invoice);
        observable.subscribe(token => {
            if (token.accountCategory !== 'PARTICIPANT') {
                let modal = this.modalService.open(CustomErrorModal);
                modal.componentInstance.exception = 'Данная учетная запись не принадлежит участнику'
                return;
            }
            this.storeTokenPayload(token.payload);
            this.authenticated.next(true);
            done();
        });
    }

    loginWithGoogle(googleIdToken: string, done): void {
        const invoice = {
            method: 'GOOGLE',
            googleIdToken: googleIdToken
        };
        const url = this.configurationService.getApiBaseUrl() + '/security/tokens/';
        this.http.post<SecurityToken>(url, invoice).subscribe(token => {
            if (token.accountCategory !== 'PARTICIPANT') {
                let modal = this.modalService.open(CustomErrorModal);
                modal.componentInstance.exception = 'Данная учетная запись не принадлежит участнику';
                return;
            }
            this.storeTokenPayload(token.payload);
            this.authenticated.next(true);
            done();
        });
    }

    logout(done): void {
        const url = this.configurationService.getApiBaseUrl() + '/security/tokens/current';
        this.removeFirstUrl();
        const observable = this.http.delete(url);
        observable.subscribe(() => {
            this.removeTokenPayload();
            //this.authenticated.next(false);
            if (gapi.auth2) {
                const auth2 = gapi.auth2.getAuthInstance();
                if (auth2) {
                    auth2.signOut().then(() => {
                        this.ngZone.run(() => {
                            this.authenticated.next(false);
                            done();
                        });
                    }, () => {
                        this.authenticated.next(false);
                        done();
                    });
                } else {
                    this.authenticated.next(false);
                    done();
                }
            } else {
                this.authenticated.next(false);
                done();
            }
        }, () => {
            this.removeTokenPayload();
            done();
        });
    }


    signup(quickname: string, email: string): Observable<{}> {
        const invoice = {
            email: email,
            quickname: quickname,
            backlinkPathTemplate: this.backlinkTemplate
        };
        const url = this.configurationService.getApiBaseUrl() + '/security/registrations/';
        return this.http.post<SecurityToken>(url, invoice)
    }

    isEmailFree(email: string): Observable<Boolean> {
        const url = this.configurationService.getApiBaseUrl() + '/security/registrations/emailValidator?email='
            + email;
        return this.http.get<Boolean>(url, {headers: {ignoreLoadingBar: '', ignoreError: ''}});
    }

    retrieveVoucherByPayload(payload: string, fail): Observable<SecurityVoucher> {
        const url = this.configurationService.getApiBaseUrl() + '/security/vouchers/?payload=' + payload;
        return this.http.get<SecurityVoucher>(url)
            .catch(() => {
                fail();
                return Observable.of(null);
            })
    }

    verifyAccountByVoucher(payload: string, password: string, done, fail) {
        const invoice = {
            method: 'VOUCHER',
            voucherPayload: payload,
            passwordUpdate: password
        };
        console.log('used' + payload);
        const url = this.configurationService.getApiBaseUrl() + '/security/tokens/';
        const observable = this.http.post<SecurityToken>(url, invoice);
        observable.subscribe(token => {
            console.log('created' + token.payload);
            this.storeTokenPayload(token.payload);
            done();
        });
    }

    resetAccount(email: string, done, fail) {
        const url = this.configurationService.getApiBaseUrl() + '/security/vouchers/';
        const invoice = {
            email: email,
            resetPassword: true,
            backlinkTemplate: this.backlinkTemplate
        };

        this.http.post<SecurityVoucher>(url, invoice).subscribe(v => {
            console.log("Reset account, create voucher", v);
            done()
        });

    }

    resendLetter(email: string, done, fail) {
        const url = this.configurationService.getApiBaseUrl() + '/security/vouchers/';
        const invoice = {
            email: email,
            resetPassword: false,
            resend: true,
            backlinkTemplate: this.backlinkTemplate
        };
        this.http.post<SecurityVoucher>(url, invoice).subscribe(v => {
            done();
        });
    }

    checkAccessToPreviousBook(): Observable<Workbook> {
        const url = this.configurationService.getApiBaseUrl() + '/workbooks/' + (this.storage.getItem('lastWorkbookId') || 'no-id');
        return this.http.get<Workbook>(url, {headers: {ignoreError: ''}});
    }

    private loadTokenPayload(): string {
        return this.storage.getItem('tchallenge-participant-security-token-payload');
    }

    private storeTokenPayload(payload: string): void {
        this.storage.setItem('tchallenge-participant-security-token-payload', payload);
    }

    private removeTokenPayload(): void {
        this.storage.removeItem('tchallenge-participant-security-token-payload');
    }

    saveFirstUrl(firstUrl: string) {
        this.storage.setItem('tchallenge-participant-first-url', firstUrl);
    }

    get firstUrl(): string {
        return this.storage.getItem('tchallenge-participant-first-url');
    }

    removeFirstUrl() {
        this.storage.removeItem('tchallenge-participant-first-url')
    }

    loginWithVk(session, done: () => void) {
        const invoice = {
            method: 'VK',
            vkSession: {
                expire: session.expire,
                mid: session.mid,
                secret: session.secret,
                sig: session.sig,
                sid: session.sid,
                userId: session.user.id,
                firstName: session.user.first_name,
                lastName: session.user.last_name
            }
        };
        const url = this.configurationService.getApiBaseUrl() + '/security/tokens/';
        this.http.post<SecurityToken>(url, invoice).subscribe(token => {
            if (token.accountCategory !== 'PARTICIPANT') {
                let modal = this.modalService.open(CustomErrorModal);
                modal.componentInstance.exception = 'Данная учетная запись не принадлежит участнику';
                return;
            }
            this.storeTokenPayload(token.payload);
            this.authenticated.next(true);
            done();
        });
    }
}
