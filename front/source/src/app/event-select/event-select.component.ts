import {Component, OnInit} from '@angular/core';
import {EventService} from '../event/event.service';
import {ForumEvent} from '../event/event.model';
import {Router} from '@angular/router';
import * as moment from "moment";
import {SecurityService} from '../shared/security.service';
import {Select2Component} from 'ng2-select2';


@Component({
    selector: 'app-event-select',
    templateUrl: './event-select.component.html',
    styleUrls: ['./event-select.component.scss']
})
export class EventSelectComponent implements OnInit {
    private static readonly pageSize = 10;
    private availableEvents: ForumEvent[];
    selectedEvent: ForumEvent;
    selectOptions = {
        ajax: this.getSelect2Ajax(),
        minimumResultsForSearch: -1,
        width: '100%'
    };

    constructor(private router: Router,
                private eventService: EventService,
                private securityService: SecurityService) {
        // fix library bug
        Select2Component.prototype.ngOnDestroy = function() {
            if (this.element && this.element.off) {
                this.element.off('select2:select select2:unselect');
            }
        };
    }

    ngOnInit() {
        this.availableEvents = [];
        this.eventService.retrieveActive().subscribe(events => {
            if (events.items.length) {
                events.items.forEach(value => {
                    value['text'] = value.caption;
                    this.availableEvents.push(value);
                });
                this.selectedEvent = this.availableEvents[0];
            }
        });

    }

    onApplyClicked($event: Event): void {
        $event.preventDefault();
        this.router.navigate(['events/' + this.selectedEvent.permalink]);
    }

    formatDate(date): String {
        return moment(date).format("DD.MM.YYYY HH:mm");
    }

    getSelect2Ajax(): Select2AjaxOptions {

        return {
            url: this.eventService.getActiveEventsUrl(),
            headers: {'Authorization': `BEARER ` + this.securityService.getCurrentToken()},
            dataType: 'json',
            data: (params) => {
                return {
                    pageSize: EventSelectComponent.pageSize,
                    pageIndex: params.page ? params.page : 0
                };
            },
            processResults: function(data, params) {
                params.page = params.page || 0;

                return {
                    results: data.items.map(event => {
                        event['text'] = event.caption;
                        return event;
                    }),
                    pagination: {
                        more: ((params.page + 1) * EventSelectComponent.pageSize) < data.total
                    }
                };
            }
        } as Select2AjaxOptions;
    }

    select(event) {
        this.selectedEvent = event.data[0];
    }
}
