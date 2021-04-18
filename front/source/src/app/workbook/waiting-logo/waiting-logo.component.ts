import { Component} from '@angular/core';
import {trigger, state, style, animate, transition} from '@angular/animations';



@Component({
  selector: 'app-waiting-logo',
  templateUrl: './waiting-logo.component.html',
  styleUrls: ['./waiting-logo.component.scss'],
  animations: [
    trigger('animate', [
      state('backway', style({
        opacity: 1,
      })),
      state('oneway', style({
        opacity: 0,
      })),
      transition('oneway => backway', [
        animate('1s')
      ]),
      transition('backway => oneway', [
        animate('1s')
      ])
    ])
  ]
})

export class WaitingLogoComponent{
    animates: Array<String> = [ "oneway", "oneway", "oneway", "oneway", "oneway", "oneway" ]; 

    currentDotIndex = 0;
    animationTimer = setInterval(() => {
      if( this.animates[ this.currentDotIndex ] == "oneway" ){
        this.animates[ this.currentDotIndex ] = "backway";
      }
      else{
        this.animates[ this.currentDotIndex ] = "oneway";
      }
      ( this.currentDotIndex < 6 )? this.currentDotIndex++ : this.currentDotIndex = 0;
    }, 1000);
}
