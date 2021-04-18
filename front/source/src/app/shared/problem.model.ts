import {ProblemOption} from './problem-option.model';
import {ProblemSnippet} from './problem-snippet.model';
import {ProblemImage} from "./problem-image.model";

export class Problem {

    id?: string;
    categories: string[];
    complexity: number;
    difficulty: string;
    expectation: string;
    introduction?: string;
    options?: ProblemOption[];
    question: string;
    snippets?: ProblemSnippet[];
    images?: ProblemImage[];
    status?: string;
    codeExpectationItems?: ProblemCodeExpectationItems;
}

export class ProblemCodeExpectationItems{
    predefinedCode: string;
    predefinedLang: string;
    enableTestsRun: boolean;
    contest: ContestInvoice;
}

export class ContestInvoice {
    name: string;
    timeLimit: number;
    memoryLimit: number;
    tests: TestInvoice[];
}

export class TestInvoice {
    input: string;
    output: string;
}
