import {NgModule} from '@angular/core';
import {SharedModule} from "../shared/shared.module";
import {UserStatisticsService} from "./user-statistics.service";
import {UserStatisticsComponent} from "./user-statistics.component";
import {NgxChartsModule} from "@swimlane/ngx-charts";
import {NgxLoadingModule,  ngxLoadingAnimationTypes} from 'ngx-loading';

@NgModule({
    imports: [
        SharedModule,
        NgxChartsModule,
        NgxLoadingModule.forRoot({
            animationType: ngxLoadingAnimationTypes.circle,
            primaryColour: '#ffffff',
            secondaryColour: '#cccccc',
            backdropBackgroundColour: '#ffffff',
            backdropBorderRadius: '3px'
        })
    ],
    declarations: [
        UserStatisticsComponent
    ],
    exports: [],
    providers: [UserStatisticsService]
})
export class UserStatisticsModule {

}
