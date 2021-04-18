import {Assignment} from './assignment.model';
import {EventCongratulationMessage} from '../event/event.model';

export class Workbook {

    id?: string;
    textcode: string;
    assignments: Assignment[];
    status: WorkbookStatus;
    reviewed: boolean;
    coworkerLink: string;
    createdAt: string;
    assessedAt: string;
    submittableUntil: string;
    eventId: string;
    avgScore?: number;
}


export enum WorkbookStatus {
    CREATED,
    APPROVED,
    DISCARDED,
    SUBMITTED,
    ASSESSED,
    DELETED
}

export class WorkbookInfo {
    workbook: Workbook;
    active: boolean;
    eventCaption: string;
    congratulations: EventCongratulationMessage[];
    reviewThreshold: number;
}
