import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/operator/catch';


import {HttpClient} from '@angular/common/http';
import {ForumEvent} from './event.model';
import {EventSearchResult} from './event-search-result.model';
import {Specialization} from './specialization.model';
import {ConfigurationService} from '../shared/configuration.service';

@Injectable()
export class EventService {

    constructor(private http: HttpClient,
                private configurationService: ConfigurationService) {

    }

    retrieveById(id: string): Observable<ForumEvent> {
        const url = this.configurationService.getApiBaseUrl() + '/events/';
        return this.http.get<ForumEvent>(url + id)
    }

    searchByPermalink(permalink: string): Observable<EventSearchResult> {
        const url = this.configurationService.getApiBaseUrl() + '/events/active?permalink=' + permalink;
        return this.http.get<EventSearchResult>(url)
    }

    retrieveByPermalink(permalink: string): Observable<ForumEvent> {
        const url = this.configurationService.getApiBaseUrl() + '/events/bypermalink?permalink=' + permalink;
        return this.http.get<ForumEvent>(url)
    }

    retrieveSpecializations(): Observable<Specialization[]> {
        const url = this.configurationService.getApiBaseUrl() + '/specializations/';
        return this.http.get<Specialization[]>(url)
    }

    retrieveMaturities(): Observable<string[]> {
        const url = this.configurationService.getApiBaseUrl() + '/maturities/';
        return this.http.get<string[]>(url)
    }

    retrieveActive(): Observable<EventSearchResult> {
        const url = this.configurationService.getApiBaseUrl() + '/events/active?permalink=.*&detailed=true';
        return this.http.get<EventSearchResult>(url)
    }

    getActiveEventsUrl(): string {
        return this.configurationService.getApiBaseUrl() + '/events/active?detailed=true';
    }
}
