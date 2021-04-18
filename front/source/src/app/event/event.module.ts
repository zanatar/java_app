import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';

import {SharedModule} from '../shared/shared.module';
import {EventService} from './event.service';
import {EventViewComponent} from './event-view/event-view.component';

@NgModule({
    imports: [
        BrowserModule,
        SharedModule
    ],
    declarations: [
        EventViewComponent
    ],
    exports: [
    ],
    providers: [
        EventService
    ]
})
export class EventModule {

}
