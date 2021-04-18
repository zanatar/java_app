import {NgModule} from '@angular/core';
import {WorkbookListComponent} from './workbook-list/workbook-list.component';
import {WorkbookItemComponent} from './workbook-item/workbook-item.component';
import {AllWorkbooksComponent} from './all-workbooks.component';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';
import {EventModule} from '../event/event.module';
import {WorkbookModule} from '../workbook/workbook.module';
import {WorkbookFilterPipe} from './workbook-list/workbook-filter.pipe';

@NgModule({
    imports: [
        CommonModule,
        EventModule,
        FormsModule,
        ReactiveFormsModule,
        WorkbookModule
    ],
    declarations: [
        AllWorkbooksComponent,
        WorkbookListComponent,
        WorkbookItemComponent,
        WorkbookFilterPipe
    ]
})
export class AllWorkbooksModule {
}
