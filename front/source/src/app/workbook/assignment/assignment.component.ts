import {Component, Input} from '@angular/core';
import {Assignment} from '../assignment.model';
import {CodeLanguageService} from '../../shared/code-language.service';

@Component({
    selector: 'app-assignment',
    templateUrl: './assignment.component.html',
    styleUrls: ['./assignment.component.css']
})
export class AssignmentComponent {

    @Input() last: boolean;
    @Input() assignment: Assignment;
    @Input() classified: boolean;
    @Input() workbookId: string;

    @Input() listDetail: boolean;

    constructor(private codeLanguageService: CodeLanguageService) {

    }

    get solved(): boolean {
        return !this.classified && (this.assignment.score === this.assignment.scoreMax);
    }

    get wrong(): boolean {
        return !this.classified && (this.assignment.score !== this.assignment.scoreMax);
    }
}
