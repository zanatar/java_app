import {Component, HostListener, OnDestroy, OnInit} from '@angular/core';
import {SecurityService} from '../shared/security.service';
import {Router} from '@angular/router';
import {UserStatisticsService} from '../user-statistics/user-statistics.service';
import {WorkbookStateService} from '../workbook/workbook-state.service';
import {Subscription} from 'rxjs/Subscription';
import {WorkbookService} from '../workbook/workbook.service';
import {Observable} from 'rxjs/Observable';
import {UIUtils} from '../shared/ui-utils';

@Component({
    selector: 'app-header',
    templateUrl: './header.component.html',
    styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit, OnDestroy {

    authenticated: boolean;
    hasStatistics: boolean;
    hasWorkbooks: Observable<boolean>;

    private workbookCreated: Subscription;
    private authChanged: Subscription;
    private screenWidth: number;


    constructor(private securityService: SecurityService,
                private router: Router,
                private statService: UserStatisticsService,
                private workbookStateService: WorkbookStateService,
                private workbookService: WorkbookService) {

    }

    ngOnInit(): void {
        this.setAuth();
        this.authChanged = this.securityService.authenticated.subscribe(auth => {
            this.authenticated = auth;
                if (auth) {
                    this.setHasStat();
                    this.hasWorkbooks = this.workbookService.areWorkbooksAvailable();
                }
            }
        );
        this.workbookCreated = this.workbookStateService.workbookCreated.subscribe(() => {
            this.setHasStat();
        });
        this.screenWidth = window.innerWidth;
    }



    onHome(): void {
        this.router.navigate(['/']);
    }

    onLogout(): void {
        this.securityService.logout(() => this.router.navigate(['login']));
        // this.setAuth();
    }

    private setAuth() {
        this.securityService.isAuthenticated().subscribe(auth => {
            this.authenticated = auth;
            if (this.authenticated) {
                this.setHasStat();
                this.hasWorkbooks = this.workbookService.areWorkbooksAvailable();
            }
        });
    }

    private setHasStat() {
        this.statService.hasStat().subscribe(hasStat => {
            this.hasStatistics = hasStat;
        });
    }

    ngOnDestroy(): void {
        this.authChanged.unsubscribe();
        this.workbookCreated.unsubscribe();
    }

    @HostListener('window:resize') onResize() {
        this.screenWidth = window.innerWidth;
    }

    onMobile() {
        return !UIUtils.hasEnoughWidth(this.screenWidth);
    }
}
