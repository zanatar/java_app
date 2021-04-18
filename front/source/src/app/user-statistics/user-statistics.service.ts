import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {ConfigurationService} from "../shared/configuration.service";
import {Observable} from "rxjs";
import 'rxjs/add/operator/catch';

import {GeneralUserStatistics} from "./general-user-statistics.model";

@Injectable()
export class UserStatisticsService {

    constructor(private http: HttpClient,
                private configurationService: ConfigurationService) {

    }

    getStat(): Observable<GeneralUserStatistics> {
        const url = this.configurationService.getApiBaseUrl() + '/statistics/';
        return this.http.get<GeneralUserStatistics>(url)
    }

    hasStat(): Observable<boolean> {
        const url = this.configurationService.getApiBaseUrl() + '/statistics/presence/';
        return this.http.get<boolean>(url,   {headers: { ignoreLoadingBar: '' }})
    }

}