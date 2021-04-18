import {Component, Input, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';

import {EventService} from '../event.service';
import {ForumEvent} from '../event.model';
import {Specialization} from '../specialization.model';
import {WorkbookService} from '../../workbook/workbook.service';
import {WorkbookInvoice} from '../../workbook/workbook-invoice.model';
import {SecurityService} from '../../shared/security.service';

@Component({
    selector: 'app-event-view',
    templateUrl: './event-view.component.html',
    styleUrls: ['./event-view.component.css']
})
export class EventViewComponent implements OnInit {

    @Input() event: ForumEvent;

    allMaturities: string[];
    availableSpecializations: Specialization[];
    availableMaturities: string[];
    selectedMaturity: string;
    selectedSpecialization: Specialization;

    constructor(private route: ActivatedRoute,
                private router: Router,
                private securityService: SecurityService,
                private eventService: EventService,
                private workbookService: WorkbookService) {

    }

    ngOnInit(): void {
        this.eventService.retrieveMaturities().subscribe(maturities => {
            this.allMaturities = maturities;
        });
        this.availableSpecializations = [];
        this.eventService.retrieveSpecializations().subscribe(specializations => {
            this.route.params.subscribe((params) => {
                const permalink = params['permalink'];
                this.eventService.retrieveByPermalink(permalink).subscribe(event => {
                    this.event = event;
                     this.event.specializationPermalinks.forEach(s => {
                         const spec: Specialization = specializations.filter(sp => sp.permalink === s)[0];
                        this.availableSpecializations.push(spec);
                    });
                    this.availableMaturities = this.allMaturities
                        .filter(m => -1 !== event.maturities.indexOf(m));
                    this.selectedMaturity = this.availableMaturities[0];
                    this.selectedSpecialization = this.availableSpecializations[0];
                });
            });
        });
    }

    wordFormatter(word: string): string {
        return word[0] + word.substring(1).toLowerCase();
    }

    onApplyClicked($event: Event): void {
        $event.preventDefault();
        const workbook: WorkbookInvoice = {
            eventId: this.event.id,
            maturity: this.selectedMaturity,
            specializationPermalink: this.selectedSpecialization.permalink
        };
        this.workbookService.create(workbook).subscribe(idAware => {
            this.router.navigate(['workbooks/' + idAware.id]);
        });
    }
}
