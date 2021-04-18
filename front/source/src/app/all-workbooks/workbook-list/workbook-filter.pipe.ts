import {Pipe, PipeTransform} from "@angular/core";
import {WorkbookInfo} from '../../workbook/workbook.model';

@Pipe({
    name: 'workbookFilter'
})
export class WorkbookFilterPipe implements PipeTransform {
    transform(items: WorkbookInfo[], searchText: string): any {
        if (!items) return [];
        if (!searchText) return items;

        return items.filter(workbookData => {
            return workbookData.eventCaption &&
                workbookData.eventCaption.toLowerCase().indexOf(searchText.toLowerCase()) !== -1
        })
    }

}