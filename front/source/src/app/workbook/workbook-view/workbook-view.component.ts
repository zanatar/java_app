import {Component, Input, OnDestroy, OnInit, ViewChildren} from '@angular/core';
import {WorkbookService} from '../workbook.service';
import {Workbook, WorkbookStatus} from '../workbook.model';
import {ActivatedRoute, Router} from '@angular/router';
import {Assignment} from '../assignment.model';
import {AssignmentComponent} from '../assignment/assignment.component';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {EmptySolutionModal} from './empty-solution-modal/empty-solution-modal';
import {WorkbookStateService} from '../workbook-state.service';
import {Subscription} from 'rxjs/Subscription';

@Component({
    selector: 'app-workbook-view',
    templateUrl: './workbook-view.component.html',
    styleUrls: ['./workbook-view.component.css']
})
export class WorkbookViewComponent implements OnInit, OnDestroy {

    @Input() workbook: Workbook;
    @Input() listDetail: boolean;

    corrects: number;
    total: number;

    @ViewChildren(Assignment) private assignmentComponents: AssignmentComponent;

    private storage = localStorage;
    private updReviewedInterval;
    private updAssessedInterval;
    private workbookSet: Subscription;

    constructor(private route: ActivatedRoute,
                private workbookService: WorkbookService,
                private router: Router,
                private modalService: NgbModal,
                private workbookStateService: WorkbookStateService) {

    }

    ngOnInit(): void {
        if (!this.listDetail) {
            this.route.params.subscribe((params) => {
                const workbookId = params['id'];
                this.reinit(workbookId, false);
                this.updReviewedInterval = setInterval(() => {
                    this.workbookService.isReviewed(workbookId).subscribe(reviewed => {
                        this.workbook.reviewed = reviewed;
                        if (reviewed) {
                            clearInterval(this.updReviewedInterval);
                        }
                    },
                    error => {
                        clearInterval(this.updReviewedInterval);                  
                    });
                }, 5000);

            });
        } else {
            if (this.workbook) {
                this.corrects = 0;
                this.total = 0;
                this.reinit(this.workbook.id, false);
            }
            this.workbookSet = this.workbookStateService.workbookSet
                .subscribe(workbookInfo => {
                    this.corrects = 0;
                    this.total = 0;
                    this.reinit(workbookInfo.workbook.id, false);
                });
            this.updReviewedInterval = setInterval(() => {
                if (this.workbook) {
                    this.workbookService.isReviewed(this.workbook.id).subscribe(reviewed => {
                        this.workbook.reviewed = reviewed;
                        if (reviewed) {
                            clearInterval(this.updReviewedInterval);
                        }
                    },
                    error => {
                        clearInterval(this.updReviewedInterval);                  
                    });
                }
            }, 5000);
        }
    }

    private get emptySolution() {
        let testAssignments: Array<Assignment> = this.workbook.assignments
            .filter(a => a.problem.expectation === 'SINGLE' || a.problem.expectation === 'MULTIPLE')
            .filter(a =>
                a.problem.options.filter(o => o.checked).length === 0);
        let textAssignments: Array<Assignment> = this.workbook.assignments
            .filter(a => a.problem.expectation === 'TEXT')
            .filter(a => a.solution == "" || a.solution == null);
        let testAndtextAssignments: Array<Assignment> = testAssignments.concat(textAssignments);
        return(testAndtextAssignments);
    }

    onSubmit(): void {
        if (this.hasEmptySolution()) {
            this.modalService.open(EmptySolutionModal).result.then(solve => {
                if (solve) {
                    console.log(this.workbook);

                    this.scrollToElement('assignment' + this.emptySolution[0].index);
                } else {
                    this.submitWorkbook();
                }
            });
        } else {
            this.submitWorkbook();
        }
    }

    private scrollToElement(id: string) {
        const elementList = document.querySelectorAll('#' + id);
        const element = elementList[0] as HTMLElement;
        if (element) {
            element.scrollIntoView({behavior: 'smooth'});
        }
    }

    private submitWorkbook() {
        this.workbookService.updateStatus(this.workbook.id, 'SUBMITTED').subscribe( s => {
            this.workbookStateService.notifyWorkbookSubmitted();
            this.workbook.status = (<any>WorkbookStatus)[s];
            this.workbookStateService.notifyWorkbookUpdated(this.workbook);
            console.log("Checking if assessed...");
            this.updAssessedInterval = setInterval(() => {
                this.workbookService.isAssessed(this.workbook.id).subscribe(assessed => {
                    if (assessed) {
                        console.log("Assessed!");
                        clearInterval(this.updAssessedInterval);
                           this.workbookService.retrieveById(this.workbook.id).subscribe(workbook => {
                            this.workbook = <Workbook>workbook;
                            this.storage.setItem('lastWorkbookId', this.workbook.id);
                            this.workbook.status = (<any>WorkbookStatus)[this.workbook.status];
                        });                  
                    }
                    else{
                        console.log("Current status is " + s);
                    }
                },

                error => {
                    console.log("Workbook disappeared! HTTP status is " + error.status + ". Deleting assessment checking");
                    clearInterval(this.updAssessedInterval);                  
                });
            }, 8000);
        });
    }

    private hasEmptySolution() {
        return this.emptySolution.length !== 0;
    }

    onOnceAgain(): void {
        this.router.navigate(['/']);
    }

    private reinit(id: string, afterSubmit: boolean): void {
        this.corrects = 0;
        this.total = 0;
        this.workbookService.retrieveById(id, () => this.router.navigate(['/'])).subscribe(workbook => {
            this.onRetrieved(workbook, afterSubmit);
        });
    }

    private onRetrieved(workbook, afterSubmit: boolean) {
        this.storage.setItem('lastWorkbookId', workbook.id);
        this.workbook = workbook;
        if ((this.workbook.status !== WorkbookStatus.SUBMITTED) && (this.workbook.status !== WorkbookStatus.APPROVED)) {
            this.workbook.status = (<any>WorkbookStatus)[this.workbook.status];
        }
        this.total = workbook.assignments.length;
        if (workbook.status === WorkbookStatus.SUBMITTED) {
            workbook.assignments.forEach((a) => {
                if (a.score === a.scoreMax) {
                    this.corrects++;
                }
            });
            if (afterSubmit) {
                this.router.navigate(['/workbooks', workbook.id, 'result']);
            }
        }
    }

    get submitted() {
        return WorkbookStatus.SUBMITTED;
    }

    get assessed() {
        return WorkbookStatus.ASSESSED;
    }

    viewResult() {
        if (this.listDetail) {
            this.workbookStateService.displayWorkbookSolution(false);
        }
    }

    ngOnDestroy(): void {
        clearInterval(this.updReviewedInterval);
        if (this.workbookSet) {
            this.workbookSet.unsubscribe();
        }
    }


}
