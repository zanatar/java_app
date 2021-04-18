import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {ObjectUtils} from '../../shared/object-utils.service';
import {Subscription} from 'rxjs/Subscription';
import {WorkbookSearchResult} from '../workbook-search-result.model';
import {WorkbookInfo} from '../../workbook/workbook.model';
import {WorkbookService} from '../../workbook/workbook.service';
import {WorkbookStateService} from '../../workbook/workbook-state.service';

@Component({
    selector: 'app-workbook-list',
    templateUrl: './workbook-list.component.html',
    styleUrls: ['./workbook-list.component.css']
})
export class WorkbookListComponent implements OnInit, OnDestroy {
    workbooksData: WorkbookInfo[] = [];
    lastWorkbookData: WorkbookInfo;
    private workbookUpdated: Subscription;
    private workbookSet: Subscription;
    private solutionUpdated: Subscription;
    private codeSolutionUpdated: Subscription;
    filterText: string;
    private index = 0;
    private pageSize = 20;
    private loadedAll = false;
    @Input() onMobile: boolean;


    constructor(private workbookService: WorkbookService,
                private workbookStateService: WorkbookStateService) {

    }

    ngOnInit(): void {
        this.reload(true);
        this.workbookUpdated = this.workbookStateService.workbookUpdated.subscribe(w => {
            this.lastWorkbookData.workbook.reviewed = w.reviewed;
            const workbookData = this.workbooksData.filter(e => e.workbook.id === this.lastWorkbookData.workbook.id)[0];
            workbookData.workbook = w;
        });
        this.workbookSet = this.workbookStateService.workbookSet.subscribe(workbook => {
            this.onWorkbookSet(workbook);
        });
        this.solutionUpdated = this.workbookStateService.solutionUpdated.subscribe(updated => {
            const workbookData = this.workbooksData.filter(e => e.workbook.id === this.lastWorkbookData.workbook.id)[0];
            if (workbookData.workbook.assignments) {
                const assignment = workbookData.workbook.assignments.find(value => value.index === updated.assignmentIndex);
                assignment.solution = updated.solution;
            }
        });
        this.codeSolutionUpdated = this.workbookStateService.codeSolutionUpdated.subscribe(updated => {
            const workbookData = this.workbooksData.filter(e => e.workbook.id === this.lastWorkbookData.workbook.id)[0];
            const assignment = workbookData.workbook.assignments.find(value => value.index === updated.assignmentIndex);
            assignment.codeSolution.language = updated.language;
            assignment.codeSolution.code = updated.code;
        });
    }

    onWorkbookSelected(workbook: WorkbookInfo): void {
        const cloned = ObjectUtils.clone(workbook);
        this.workbookStateService.displayFullInfo(true);
        this.workbookStateService.notifyWorkbookSet(cloned);
    }

    onWorkbookSet(workbookInfo: WorkbookInfo): void {
        this.workbookStateService.displayFullInfo(true);
        if (this.lastWorkbookData) {
            this.lastWorkbookData.active = false;
        }
        const sourceWorkbook = this.workbooksData.filter(e => e.workbook.id === workbookInfo.workbook.id)[0];
        sourceWorkbook.active = true;
        // setTimeout(() => this.scrollTo('workbook-' + this.workbooksData.indexOf(sourceWorkbook)), 200);
        this.lastWorkbookData = sourceWorkbook;
    }

    private reload(selectFirst: boolean): void {
        this.workbookService.get(this.index++, this.pageSize, this.filterText)
            .subscribe(searchResult =>
                this.handleSearchResult(searchResult, selectFirst));
    }

    private handleSearchResult(searchResult: WorkbookSearchResult, selectFirst: boolean): void {
        this.workbooksData = [...this.workbooksData, ...searchResult.items];
        if ((this.workbooksData.length === searchResult.total) && !this.filterText) {
            this.loadedAll = true;
        }
        if ((this.workbooksData.length > 0) && (selectFirst) && (!this.onMobile)) {
            this.workbookStateService.notifyWorkbookSet(Object.assign(new WorkbookInfo(), this.workbooksData[0]));
        }
    }

    private scrollTo(id: string): void {
        const elementList = document.querySelectorAll('#' + id);
        const element = elementList[0] as HTMLElement;
        if (element) {
            element.scrollIntoView({behavior: 'smooth'});
        }
    }

    onScrolled(event: UIEvent): void {
        const element = event.target as Element;
        const nearBottom = element.scrollHeight - element.scrollTop <= element.clientHeight + 20;
        if (nearBottom && !this.loadedAll) {
            this.reload(false);
        }
    }

    onFilterChanged(event) {
        this.filterText = event;
        if (!this.loadedAll) {
            this.workbooksData = [];
            this.index = 0;
            this.reload(true);
        }
    }

    ngOnDestroy(): void {
        this.workbookSet.unsubscribe();
        this.workbookUpdated.unsubscribe();
        this.solutionUpdated.unsubscribe();
        this.codeSolutionUpdated.unsubscribe();
    }
}
