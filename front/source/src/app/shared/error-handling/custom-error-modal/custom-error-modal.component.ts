import {Component} from "@angular/core";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
    selector: 'app-custom-error-modal',
    templateUrl: './custom-error-modal.component.html',
    styleUrls: ['./custom-error-modal.component.css']
})
export class CustomErrorModal {
    public exception: string;

    constructor(public activeModal: NgbActiveModal) {

    }
}