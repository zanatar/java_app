import {Component, HostListener, OnDestroy, OnInit} from '@angular/core';
import {Subscription} from 'rxjs/Subscription';
import {WorkbookInfo} from '../workbook/workbook.model';
import {WorkbookStateService} from '../workbook/workbook-state.service';
import {WorkbookService} from '../workbook/workbook.service';
import {UIUtils} from '../shared/ui-utils';

@Component({
    selector: 'app-all-workbooks',
    templateUrl: './all-workbooks.component.html',
    styleUrls: ['./all-workbooks.component.scss']
})
export class AllWorkbooksComponent implements OnInit, OnDestroy {

    displayFullInfo: boolean;
    displaySolution: boolean;
    private screenHeight: number;
    private screenWidth: number;
    private showInfo: Subscription;
    private showSolution: Subscription;
    private setWorkbook: Subscription;
    private workbookUpdated: Subscription;
    private solutionUpdated: Subscription;
    private codeSolutionUpdated: Subscription;
    workbookInfo: WorkbookInfo;

    constructor(private workbookStateService: WorkbookStateService,
                private workbookService: WorkbookService) {
    }

    ngOnInit() {
        this.showInfo = this.workbookStateService.displayInfo.subscribe((e) => {
            this.displayFullInfo = e;
        });
        this.showSolution = this.workbookStateService.displaySolution.subscribe((e) => {
            this.displaySolution = e;
        });
        // this.workbookService.availableLanguages().subscribe(languages => {
        //     this.supportedLanguages = languages.map(lang => lang.language);
        // });
        this.setWorkbook = this.workbookStateService.workbookSet.subscribe(info => {
            if (info.workbook.assessedAt == null) {
                this.displaySolution = true;
            }
            this.workbookInfo = info;
        });
        this.workbookUpdated = this.workbookStateService.workbookUpdated.subscribe(w => {
            if (this.workbookInfo.workbook.id === w.id) {
                this.workbookInfo.workbook = w;
            }
        });
        this.solutionUpdated = this.workbookStateService.solutionUpdated.subscribe(updated => {
            if (this.workbookInfo.workbook.id === updated.workbookId) {
                const assignment = this.workbookInfo.workbook.assignments
                    .find(value => value.index === updated.assignmentIndex);
                assignment.solution = updated.solution;
            }
        });
        this.codeSolutionUpdated = this.workbookStateService.codeSolutionUpdated.subscribe(updated => {
            if (this.workbookInfo.workbook.id === updated.workbookId) {
                const assignment = this.workbookInfo.workbook.assignments
                    .find(value => value.index === updated.assignmentIndex);
                assignment.codeSolution.language = updated.language;
                assignment.codeSolution.code = updated.code;
            }
        });
        this.screenHeight = window.innerHeight;
        this.screenWidth = window.innerWidth;
    }

    @HostListener('window:resize') onResize() {
        this.screenHeight = window.innerHeight;
        this.screenWidth = window.innerWidth;
    }

    hasEnoughWidth(): boolean {
        return UIUtils.hasEnoughWidth(this.screenWidth);
    }

    goBack(): void {
        this.workbookStateService.displayFullInfo(false);
    }

    ngOnDestroy(): void {
        this.showInfo.unsubscribe();
        this.showSolution.unsubscribe();
        this.setWorkbook.unsubscribe();
        this.workbookUpdated.unsubscribe();
        this.solutionUpdated.unsubscribe();
        this.codeSolutionUpdated.unsubscribe();
    }

}
