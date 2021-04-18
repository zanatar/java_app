import * as moment from "moment";
import {Moment} from "moment";
import {CategoryStatistics, WorkbookScore} from "./general-user-statistics.model";

export class UserStatisticsConverter {

    static workbooksScores(stats: WorkbookScore[]) {
        let res = {
            name: "Результаты теста",
            series: []
        };

        let index = 0;
        stats.forEach(stat => {
            res.series.push({
                name: index,
                date: moment(stat.createdAt).format('DD.MM.Y HH:mm'),
                value: stat.score
            });
            index++;
        });
        return [res];
    }

    static categoryStats(stats: { [key: string]: CategoryStatistics; }, valueKey) {
        let res = [];
        for (let key in stats) {
            let value = stats[key];
            res.push({
                name: key,
                value: value[valueKey]
            })
        }
        return res;
    }

    static calendarStats(stats: WorkbookScore[]) {
        const result = [];
        const dates: Moment[] = [];
        stats.forEach(d => dates.push(moment(d.createdAt)));

        for (let i = 0; i < dates.length;) {
            // if (i) {
            //     this.addSkippedWeeks(result, dates[i])
            // }


            const curDate = dates[i];
            const monday = curDate.startOf('isoWeek');
            let activeDaysInWeek: { date: Moment, value: number }[] = [];

            while (i < dates.length && this.isSameWeek(dates[i], monday)) {
                activeDaysInWeek.push({
                    date: dates[i],
                    value: stats[dates[i].toString()]
                });
                i++;
            }

            this.addWeek(result, monday, activeDaysInWeek)
        }

        return result;
    }

    private static addWeek(result, monday: Moment, activeDates: { date: Moment, value: number }[]) {
        const series = [];

        for (let days = 0; days < 7; days++) {
            const day = monday.clone().add(days, "days");
            let value = 0;
            activeDates.forEach(d => {
                if (this.isSameDay(day, d.date) && day.isSame(d.date, "isoWeek")) {
                    value++;
                }
            });

            series.push({
                date: day.toDate(),
                name: this.dayByNumber(day.get("day")),
                value: value
            })
        }
        result.push({
            name: monday.toDate().toString(),
            series: series
        })
    }

    private static addSkippedWeeks(result, untilDate: Moment) {
        let lastActiveDay = result[result.length - 1].name;
        let curMonday = moment(lastActiveDay).startOf("isoWeek").add(1, "weeks");

        let kek = 0;
        while (!this.isSameWeek(curMonday, untilDate)) {
            this.addWeek(result, curMonday, []);
            curMonday.add(1, "weeks");
            kek++;
            if (kek > 20) {
                break;
            }
        }

    }

    private static dayByNumber(dayNumber: number): string {
        switch (dayNumber) {
            case 0:
                return "Вс";
            case 1:
                return "Пн";
            case 2:
                return "Вт";
            case 3:
                return "Ср";
            case 4:
                return "Чт";
            case 5:
                return "Пт";
            case 6:
                return "Сб";
        }
    }

    private static isSameDay(day1: Moment, day2: Moment) {
        return day1.isSame(day2, "day") &&
            day1.isSame(day2, "month") &&
            day1.isSame(day2, "year")
    }

    private static isSameWeek(day1: Moment, day2: Moment) {
        return day1.isSame(day2, "week") &&
            day1.isSame(day2, "month") &&
            day1.isSame(day2, "year")
    }
}