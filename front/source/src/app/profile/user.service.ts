import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {ConfigurationService} from "../shared/configuration.service";
import 'rxjs/add/operator/catch';
import {Observable} from "rxjs";
import {ParticipantPersonality, Personality, User} from "./user.model";

@Injectable()
export class UserService {

    constructor(private http: HttpClient,
                private configurationService: ConfigurationService) {

    }


    getCurrent(): Observable<User> {
        const url = this.configurationService.getApiBaseUrl() + '/accounts/current';
        return this.http.get<User>(url)
    }

    updateContacts(personality: ParticipantPersonality): Observable<ParticipantPersonality> {
        const url = this.configurationService.getApiBaseUrl() + '/accounts/current/participantPersonality';
        return this.http.put<ParticipantPersonality>(url, personality)
    }

    updatePersonal(personality: Personality): Observable<Personality> {
        const url = this.configurationService.getApiBaseUrl() + '/accounts/current/personality';
        return this.http.put<Personality>(url, personality)
    }

    updatePassword(current: string, desired: string) :Observable<void>{
        const url = this.configurationService.getApiBaseUrl() + '/accounts/current/password';
        const data = {
            current: current,
            desired: desired
        };
        return this.http.put<void>(url, data)
    }

    checkHasEmail(): Observable<Boolean> {
        const url = this.configurationService.getApiBaseUrl() + '/accounts/current';
        return this.http.get<User>(url, {headers: {ignoreError: ''}})
            .map(value => value.email != null);
    }

    setEmail(email: string) {
        const url = this.configurationService.getApiBaseUrl() + '/accounts/current/email';
        const invoice = {email: email};
        return this.http.put<void>(url, invoice);
    }
}