import {Subject} from 'rxjs/Subject';
import {Injectable} from '@angular/core';
import {Workbook, WorkbookInfo} from './workbook.model';

@Injectable()
export class WorkbookStateService {
    workbookCreated = new Subject<void>();
    workbookSet = new Subject<WorkbookInfo>();
    workbookUpdated = new Subject<Workbook>();
    displayInfo = new Subject<boolean>();
    displaySolution = new Subject<boolean>();
    solutionUpdated = new Subject<{workbookId: string, assignmentIndex: number, solution: string}>();
    codeSolutionUpdated = new Subject<{workbookId: string, assignmentIndex: number, code: string, language: string}>();

    notifyWorkbookSubmitted() {
        this.workbookCreated.next();
    }

    notifyWorkbookSet(workbookInfo: WorkbookInfo) {
        this.workbookSet.next(workbookInfo);
    }

    notifyWorkbookUpdated(workbook: Workbook) {
        this.workbookUpdated.next(workbook);
    }

    notifySolutionUpdated(workbookId: string, assignmentIndex: number, solution: string) {
        this.solutionUpdated.next({workbookId, assignmentIndex, solution});
    }

    notifyCodeSolutionUpdated(workbookId: string, assignmentIndex: number, code: string, language: string) {
        this.codeSolutionUpdated.next({workbookId, assignmentIndex, code, language});
    }

    displayFullInfo(display: boolean) {
        this.displayInfo.next(display);
    }

    displayWorkbookSolution(display: boolean) {
        this.displaySolution.next(display);
    }
}
