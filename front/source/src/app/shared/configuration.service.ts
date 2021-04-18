import {Injectable} from '@angular/core';
import {environment} from '../../environments/environment';

@Injectable()
export class ConfigurationService {

    private apiBaseUrl: string;
    private clientBaseUrl: string;
    private production: boolean;

    constructor() {
        this.apiBaseUrl = environment.apiBaseUrl;
        this.clientBaseUrl = environment.clientBaseUrl;
        this.production = environment.production;
    }

    getApiBaseUrl(): string {
        return this.apiBaseUrl;
    }

    getClientBaseUrl(): string {
        return this.clientBaseUrl;
    }

    isProduction(): boolean {
        return this.production;
    }
}
