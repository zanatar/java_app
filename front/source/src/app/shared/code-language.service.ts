import { Injectable } from '@angular/core';

@Injectable()
export class CodeLanguageService {
    private languages = {
        "JAVA": "Java",
        "JAVASCRIPT": "JavaScript"
    }

    private languageToCodemirrorMode = {
        "JAVA": "text/x-c++src",
        "JAVASCRIPT": "javascript"
    };

    getCodemirrorModeByLanguage(languageName: string): string {
        return this.languageToCodemirrorMode[languageName];
    }

    getLanguageName(lang: string): string {
        return this.languages[lang];
    }
}