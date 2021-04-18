import {Component, Input, OnInit} from '@angular/core';

import * as moment from 'moment';
import {WorkbookInfo} from '../../workbook/workbook.model';

@Component({
    selector: 'app-workbook-item',
    templateUrl: './workbook-item.component.html',
    styleUrls: ['./workbook-item.component.scss']
})
export class WorkbookItemComponent implements OnInit {

    @Input() workbookInfo: WorkbookInfo;

    constructor() {
    }

    ngOnInit(): void {
    }

    formatResult(): string {
        return (this.workbookInfo.workbook.avgScore * 100).toFixed(0) + '%';
    }

    formatAssessedAt(): string {
        return moment(this.workbookInfo.workbook.assessedAt).format('DD.MM.YYYY HH:mm:ss');
    }

    formatEndDate(): string {
        return moment(this.workbookInfo.workbook.submittableUntil).format('DD.MM.YYYY HH:mm:ss');
    }
}
