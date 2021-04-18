import {Problem} from '../shared/problem.model';

export class Assignment {
    index?: number;
    problem: Problem;
    score: number;
    scoreMax: number;
    solution?: string;
    codeSolution?: CodeSolution;

}


export class CodeSubmissionResult {
    languageName: string;
    language: string;
    status: CodeSubmissionStatus;
    testNumber: number;
    cmpErr: string;
    sourceCode: string;
    time: number[];
}

export enum CodeSubmissionStatus {
    WAITING_IN_QUEUE = "Ожидание запуска",
    COMPILING = "Компиляция программы",
    WRONG_ANSWER = "Неверный ответ на тест",
    OK = "Верный ответ",
    COMPILATION_ERROR = "Ошибка компиляции",
    RUNTIME_ERROR = "Ошибка исполнения",
    SERVER_ERROR = "Ошибка сервера",
    TIME_LIMIT = "Превышен лимит времени",
    MEMORY_LIMIT = "Превышен лимит памяти",
    RUNNING_TEST = "Проверка теста"
}

export class CodeSolution {
    code: string;
    language: string;
    submissionId: string;
    lastSuccessfulSubmissionId: string;
}

export class LanguageInfo {
    language: string;
    name: string;
    notes: string;
}