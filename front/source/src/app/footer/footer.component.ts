import {Component, OnInit, HostListener} from '@angular/core';
import {UIUtils} from '../shared/ui-utils';

@Component({
    selector: 'app-footer',
    templateUrl: './footer.component.html',
    styleUrls: ['./footer.component.scss']
})
export class FooterComponent implements OnInit {
    
    private screenWidth: number;

    constructor() {
    }

    ngOnInit() {
        this.screenWidth = window.innerWidth;
    }

    @HostListener('window:resize') onResize() {
        this.screenWidth = window.innerWidth;
    }
    
    onMobile() {
        return !UIUtils.hasEnoughWidth(this.screenWidth);
    }

}
