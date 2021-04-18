import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';

import {SharedModule} from '../shared/shared.module';
import {WorkbookService} from './workbook.service';
import {AssignmentComponent} from './assignment/assignment.component';
import {WorkbookViewComponent} from './workbook-view/workbook-view.component';
import {OptionGroupComponent} from './assignment/option-group/option-group.component';
import {HighlightModule} from "ngx-highlightjs";
import {EmptySolutionModal} from "./workbook-view/empty-solution-modal/empty-solution-modal";
import {WorkbookStateService} from "./workbook-state.service";
import {WorkbookResultComponent} from '../workbook-result/workbook-result.component';
import { WaitingLogoComponent } from './waiting-logo/waiting-logo.component';

import { FormsModule } from '@angular/forms';
import { CodemirrorModule } from '@ctrl/ngx-codemirror';
import { CodeLanguageService } from '../shared/code-language.service';


@NgModule({
    imports: [
        BrowserModule,
        
        FormsModule,
        CodemirrorModule,

        SharedModule,
        HighlightModule.forRoot({theme: 'atelier-forest-light'})
    ],
    declarations: [
        WorkbookViewComponent,
        WorkbookResultComponent,
        AssignmentComponent,
        OptionGroupComponent,
        EmptySolutionModal,
        WaitingLogoComponent
    ],
    exports: [
        WorkbookViewComponent,
        WorkbookResultComponent
    ],
    providers: [
        WorkbookService,
        WorkbookStateService,
        CodeLanguageService
    ],
    entryComponents: [
        EmptySolutionModal
    ]
})
export class WorkbookModule {

}
