import {Component, HostListener, OnInit} from '@angular/core';
import {UserStatisticsService} from './user-statistics.service';
import {GeneralUserStatistics} from './general-user-statistics.model';
import {UserStatisticsConverter} from './user-statistics.converter';
import * as shape from 'd3-shape';
import * as moment from 'moment';

@Component({
    selector: 'app-user-statistics',
    templateUrl: './user-statistics.component.html',
    styleUrls: ['./user-statistics.component.scss']
})
export class UserStatisticsComponent implements OnInit {
    loading = true;
    stats: GeneralUserStatistics;
    workbooksScores;
    categoriesScores;
    categoriesNumber;
    specsStats;
    specsResultStats;
    eventsStats;
    curve;
    colorScheme;
    rowView;
    calendarRowView;
    halfRowView;
    calendarData;
    heatmapColors;

    constructor(private userStatisticsService: UserStatisticsService) {
        this.colorScheme = {
            domain: ['#5AA454',
                '#A10A28',
                '#C7B42C',
                '#AAAAAA',
                '#cb34b5',
                '#0762f8',
                '#a55a6d',
                '#32cd8b',
                '#ff8000'
            ]
        };
        this.heatmapColors = {
            domain: [
                '#808080',
                '#867979',
                '#8c7373',
                '#936c6c',
                '#996666',
                '#9f6060',
                '#a65959',
                '#ac5353',
                '#b34d4d',
                '#b94646',
                '#bf4040',
                '#c63939'
            ]
        };

        this.setRowView();
        this.curve = shape.curveMonotoneX;
    }

    static getMonthStr(month: number): string {
        switch (month) {
            case 0:
                return 'Январь';
            case 1:
                return 'Февраль';
            case 3:
                return 'Апрель';
            case 4:
                return 'Март';
            case 5:
                return 'Июнь';
            case 6:
                return 'Июль';
            case 7:
                return 'Август';
            case 8:
                return 'Сентябрь';
            case 9:
                return 'Октябрь';
            case 10:
                return 'Ноябрь';
            case 11:
                return 'Декабрь';
        }
    }

    @HostListener('window:resize')
    onResize() {
        this.setRowView();
    }

    ngOnInit() {
        this.loading = true;
        this.userStatisticsService.getStat().subscribe(stats => {
            this.stats = stats;
            this.workbooksScores = UserStatisticsConverter.workbooksScores(this.stats.workbooksScores);
            this.categoriesScores = UserStatisticsConverter.categoryStats(this.stats.problemCategoryStatistics,
                'avgScore');
            this.categoriesNumber = UserStatisticsConverter.categoryStats(this.stats.problemCategoryStatistics,
                'number');
            this.specsStats = UserStatisticsConverter.categoryStats(this.stats.specializationsStatistics,
                'number');
            this.specsResultStats = UserStatisticsConverter.categoryStats(this.stats.specializationsStatistics,
                'avgScore');
            this.eventsStats = UserStatisticsConverter.categoryStats(this.stats.eventsStatistics, 'number');
            this.calendarData = UserStatisticsConverter.calendarStats(this.stats.workbooksScores);
            this.loading = false;
        });
    }


    percentFormatter(value: number) {
        return (value * 100).toFixed(0) + '%';
    }

    setRowView() {
        if (window.innerWidth < 390){
            this.rowView = [250, 150];
            this.calendarRowView = [250, 150];
            this.halfRowView = [250, 150];
        } else if (window.innerWidth < 768) {
            this.rowView = [320, 200];
            this.calendarRowView = [320, 200];
            this.halfRowView = [320, 200];
        } else if (window.innerWidth >= 768) {
            this.rowView = [320, 200];
            this.halfRowView = [innerWidth * 0.4, 200];
            this.calendarRowView = null;
        } else {
            this.rowView = null;
            this.calendarRowView = null;
            this.halfRowView = [innerWidth * 0.4, 200];
        }
        // if (window.innerWidth < 768 || window.innerWidth >= 992) {
        //     this.rowView = [320, 200];
        //     if (window.innerWidth < 768) {
        //         this.calendarRowView = [300, 200];
        //     } else {
        //         this.calendarRowView = null;
        //     }
        // }
        // else {
        //     this.rowView = null;
        //     this.calendarRowView = null;
        // }
    }

    calendarAxisTickFormatting(mondayString: string): string {
        const lastSunday = moment(mondayString).startOf('week');
        const nextSunday = lastSunday.clone().add(1, 'weeks');
        // return lastSunday.get("month") === nextSunday.get("month")? '' :
        //     UserStatisticsComponent.getMonthStr(nextSunday.get("month"));
        return UserStatisticsComponent.getMonthStr(moment(mondayString).get('month'));
    }

    calendarTooltipText(c): string {
        return `
      <span class="tooltip-label">${c.label} • ${c.cell.date.toLocaleDateString()}</span>
      <span class="tooltip-val">Решено тестов: ${c.data.toLocaleString()}</span>
    `;
    }



}
