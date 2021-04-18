
export interface GeneralUserStatistics {
    totalWorkbooks: number;
    solvedWorkbooks: number;
    workbooksScores: WorkbookScore[];
    avgScore: number;
    totalProblemsSolved: number;
    firstAssign: string;
    lastAssign: string;
    problemCategoryStatistics: { [key: string]: CategoryStatistics; };
    eventsStatistics: { [key: string]: CategoryStatistics; };
    specializationsStatistics: { [key: string]: CategoryStatistics }
}

export interface CategoryStatistics {
    avgScore: number;
    number: number;
}

export interface WorkbookScore {
    createdAt: string;
    score: number;
}