import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import {Workbook, WorkbookStatus} from './workbook.model';
import {IdAware} from '../shared/id-aware.model';
import {HttpClient} from '@angular/common/http';
import {WorkbookInvoice} from './workbook-invoice.model';
import 'rxjs/add/operator/catch';
import {ConfigurationService} from '../shared/configuration.service';
import {CodeSubmissionResult, LanguageInfo} from "./assignment.model";
import {WorkbookSearchResult} from '../all-workbooks/workbook-search-result.model';

@Injectable()
export class WorkbookService {

    constructor(private http: HttpClient,
                private configurationService: ConfigurationService) {

    }

    create(workbook: WorkbookInvoice): Observable<IdAware> {
        workbook.backlinkPathTemplate = '/workbooks/{{id}}';
        const url = this.configurationService.getApiBaseUrl() + '/workbooks/';
        return this.http.post<IdAware>(url, workbook);
    }

    updateAssignmentSolution(id: string, index: number, solution: string): Observable<{}> {
        const data = {solution: solution};
        const url = this.configurationService.getApiBaseUrl() + '/workbooks/' + id + '/assignments/' + index;
        return this.http.put(url, data);
    }

    updateCodeSolution(id: string, index: number, code: string, language: string): Observable<Workbook> {
        const data = {
            code: code,
            language: language
        };
        const url = this.configurationService.getApiBaseUrl() + '/workbooks/' + id + '/assignments/' + index + "/code";
        return this.http.put<Workbook>(url, data);
    }

    runTests(id: string, index: number): Observable<CodeSubmissionResult> {
        const url = this.configurationService.getApiBaseUrl() + '/workbooks/' + id + '/assignments/' + index + '/tests';
        return this.http.put<CodeSubmissionResult>(url, {});
    }

    getTestsResult(id: string, index: number, lastSubmission: boolean, withSource?: boolean): Observable<CodeSubmissionResult> {
        let url = this.configurationService.getApiBaseUrl() + '/workbooks/' + id + '/assignments/' + index + '/tests'
            + '?lastSubmission=' + lastSubmission;
        if (withSource) {
            url += '&withSource=true'
        }
        return this.http.get<CodeSubmissionResult>(url, {headers: {ignoreError: ''}})
    }

    availableLanguages(): Observable<LanguageInfo[]> {
        const url = this.configurationService.getApiBaseUrl() + '/workbooks/languages';
        return this.http.get<LanguageInfo[]>(url, {headers: {ignoreError: ''}});
    }

    updateStatus(id: string, status: string): Observable<WorkbookStatus> {
        const data = {status: status};
        const url = this.configurationService.getApiBaseUrl() + '/workbooks/' + id + '/status';
        return this.http.put<WorkbookStatus>(url, data);
    }

    retrieveById(id: string, fail?): Observable<Workbook> {
        const url = this.configurationService.getApiBaseUrl() + '/workbooks/' + id;
        return this.http.get<Workbook>(url, {headers: {ignoreLoadingBar: '', ignoreError: ''}});
    }

    isReviewed(id: string): Observable<boolean> {
        const url = this.configurationService.getApiBaseUrl() + '/workbooks/' + id + '/reviewed';
        return this.http.get<boolean>(url, {headers: {ignoreLoadingBar: '', ignoreError: ''}})
    }

    isAssessed(id: string): Observable<boolean> {
        const url = this.configurationService.getApiBaseUrl() + '/workbooks/' + id + '/assessed';
        return this.http.get<boolean>(url, {headers: {ignoreLoadingBar: '', ignoreError: ''}})
    }

    get(pageIndex: number, pageSize: number, eventCaption?: string): Observable<WorkbookSearchResult> {
        let url = this.configurationService.getApiBaseUrl() + '/workbooks/?'
        + 'pageIndex=' + pageIndex + '&pageSize=' + pageSize;
        if (eventCaption) {
            url += '&eventCaption=' + eventCaption;
        }
        return this.http.get<WorkbookSearchResult>(url);
    }

    areWorkbooksAvailable(): Observable<boolean> {
        const url = this.configurationService.getApiBaseUrl() + '/workbooks/presence/';
        return this.http.get<boolean>(url,   {headers: { ignoreLoadingBar: '' }});
    }
}

