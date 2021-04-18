import {WorkbookInfo} from '../workbook/workbook.model';


export interface WorkbookSearchResult {
    items: WorkbookInfo[];
    total: number;
}