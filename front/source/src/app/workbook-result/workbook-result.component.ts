import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {Workbook, WorkbookInfo, WorkbookStatus} from '../workbook/workbook.model';
import {ActivatedRoute, Router} from '@angular/router';
import {WorkbookService} from '../workbook/workbook.service';
import * as moment from 'moment';
import {EventService} from '../event/event.service';
import {Subscription} from 'rxjs/Subscription';
import {WorkbookStateService} from '../workbook/workbook-state.service';

@Component({
    selector: 'app-workbook-result',
    templateUrl: './workbook-result.component.html',
    styleUrls: ['./workbook-result.component.scss']
})
export class WorkbookResultComponent implements OnInit, OnDestroy {

    id: string;
    scorePercent: number;
    displayScore: boolean;
    solveTime: string;
    totalProblems: number;
    @Input() workbookInfo: WorkbookInfo;
    workbook: Workbook;
    updReviewedInterval;
    currentMessage: string;
    congratulations;
    threshold: number;
    @Input() listDetail: boolean;
    private workbookSet: Subscription;

    constructor(private router: Router,
                private route: ActivatedRoute,
                private workbookService: WorkbookService,
                private workbookStateService: WorkbookStateService,
                private eventService: EventService) {

    }

    ngOnInit() {
        window.scrollTo(0, 0);
        if (!this.listDetail) {
            this.route.params.subscribe(params => {
                const id = params['id'];
                this.workbookService.retrieveById(id).subscribe(w => {
                    this.onWorkbookRetrieved(w);
                });
            });
        } else {
            if (this.workbookInfo) {
                this.congratulations = this.workbookInfo.congratulations;
                this.threshold = this.workbookInfo.reviewThreshold;
                this.workbookService.retrieveById(this.workbookInfo.workbook.id).subscribe(w => {
                    this.onWorkbookRetrieved(w);
                });
            }
            this.workbookSet = this.workbookStateService.workbookSet
                .subscribe(workbookInfo => {
                    this.congratulations = workbookInfo.congratulations;
                    this.threshold = workbookInfo.reviewThreshold;
                    this.workbookService.retrieveById(workbookInfo.workbook.id).subscribe(w => {
                        this.onWorkbookRetrieved(w);
                    });
                });
        }

        this.updReviewedInterval = setInterval(() => {
            if (this.scorePercent >= this.threshold) {
                this.workbookService.isReviewed(this.id).subscribe(reviewed => {
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

    private onWorkbookRetrieved(w) {
        this.id = w.id;
        this.workbook = w;
        if (this.workbook.status != WorkbookStatus.ASSESSED) {
            this.workbook.status = (<any>WorkbookStatus)[this.workbook.status];
        }
        this.calculateScore(w);
        if (this.listDetail && this.congratulations) {
            const congratulation = this.congratulations
                .sort((a, b) => b.threshold - a.threshold)
                .find(congratulation => congratulation.threshold <= this.scorePercent);
            if (congratulation) {
                this.currentMessage = congratulation
                    .message;
            }
        }
        if (!this.listDetail) {
            this.eventService.retrieveById(w.eventId).subscribe(event => {
                if (event.congratulations) {
                    const congratulation = event.congratulations
                        .sort((a, b) => b.threshold - a.threshold)
                        .find(congratulation => congratulation.threshold <= this.scorePercent);
                    if (congratulation) {
                        this.currentMessage = congratulation
                            .message;
                    }
                }
                this.threshold = event.reviewThreshold;
            });
        }
    }

    percentFormatter(value: number) {
        return value.toFixed(0) + '%';
    }

    durationToStr(duration: moment.Duration): string {
        if (duration.asYears() >= 1 || duration.asMonths() > 11.5) {
            return duration.asYears().toFixed(0) + ' г.';
        }
        if (duration.asMonths() >= 1 || duration.asDays() > 29.5) {
            return duration.asMonths().toFixed(0) + ' мес.';
        }
        if (duration.asDays() >= 1 || duration.asHours() > 23.5) {
            return duration.asDays().toFixed(0) + ' д.';
        }
        if (duration.asHours() >= 1 || duration.asMinutes() > 59.5) {
            return duration.asHours().toFixed(0) + ' ч.';
        }
        if (duration.asMinutes() >= 1 || duration.asSeconds() > 59.5) {
            return duration.asMinutes().toFixed(0) + ' мин.';
        }
        if (duration.asSeconds() >= 1) {
            return duration.asSeconds().toFixed(0) + ' cек.';
        }
    }

    private calculateScore(workbook: Workbook) {
        if (workbook.status === WorkbookStatus.ASSESSED) {
            this.scorePercent = workbook.avgScore * 100;
            const startedAt = moment(workbook.createdAt);
            const finishedAt = moment(workbook.assessedAt);
            this.solveTime = this.durationToStr(moment.duration(finishedAt.diff(startedAt)));
            this.totalProblems = workbook.assignments.length;
            this.displayScore = true;
        }
    }

    ngOnDestroy(): void {
        clearInterval(this.updReviewedInterval);
        if (this.workbookSet) {
            this.workbookSet.unsubscribe();
        }
    }

    viewSolution() {
        if (this.listDetail) {
            this.workbookStateService.displayWorkbookSolution(true);
        }
    }
}
