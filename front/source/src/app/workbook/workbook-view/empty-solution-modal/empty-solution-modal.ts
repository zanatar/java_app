import {Component} from "@angular/core";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
    selector: 'app-empty-solution-modal',
    templateUrl: './empty-solution-modal.html',
    styleUrls: ['./empty-solution-modal.css']
})
export class EmptySolutionModal {
    constructor(public activeModal: NgbActiveModal) {

    }
}