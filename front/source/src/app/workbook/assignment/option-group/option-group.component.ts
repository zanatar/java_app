import {Component, Input, OnInit} from '@angular/core';
import {WorkbookService} from '../../workbook.service';
import {Assignment, CodeSubmissionResult, CodeSubmissionStatus} from "../../assignment.model";
import {_} from 'underscore';
import {WorkbookStateService} from '../../workbook-state.service';
import {CodeLanguageService} from '../../../shared/code-language.service';

@Component({
    selector: 'app-option-group',
    templateUrl: './option-group.component.html',
    styleUrls: ['./option-group.component.css']
})
export class OptionGroupComponent implements OnInit {

    @Input() problemWithSolution: Assignment;
    @Input() assignmentIndex: number;
    @Input() workbookId: string;
    @Input() classified: boolean;
    oldSolution: string;

    submissionResult: CodeSubmissionResult;
    enableTestRunButton: boolean;

    private submissionTimer;
    private testRunning: boolean;

    constructor(private workbookService: WorkbookService,
                private workbookStateService: WorkbookStateService,
                private codeLanguageService: CodeLanguageService) {

    }

    hasOptions(): boolean {
        const expectation = this.problemWithSolution.problem.expectation;
        return expectation === 'SINGLE' || expectation === 'MULTIPLE';
    }

    onOptionChanged(index: number): void {
        const i = index - 1;
        const expectation = this.problemWithSolution.problem.expectation;
        if (expectation === 'SINGLE') {
            this.resetFlags();
            this.problemWithSolution.problem.options[i].checked = true;
        } else if (expectation === 'MULTIPLE') {
            this.problemWithSolution.problem.options[i].checked =
                !this.problemWithSolution.problem.options[i].checked;
        }
        const s = this.stringifySolution();
        if (s && (s !== this.oldSolution)) {
            this.oldSolution = s;
            this.workbookService.updateAssignmentSolution(this.workbookId, this.assignmentIndex, s)
                .subscribe(() => {
                    this.workbookStateService.notifySolutionUpdated(this.workbookId, this.assignmentIndex, s);
                });
        } else if (s === "") {
            this.problemWithSolution.solution = this.oldSolution;
        }
    }

    ngOnInit(): void {
        
        if (this.problemWithSolution.codeSolution) {
            if (!this.problemWithSolution.codeSolution.language) {
                this.problemWithSolution.codeSolution.language = this.problemWithSolution.problem.codeExpectationItems.predefinedLang;
            }
            if (!this.problemWithSolution.codeSolution.code) {
                this.problemWithSolution.codeSolution.code = this.problemWithSolution.problem.codeExpectationItems.predefinedCode;
            }
            this.enableTestRunButton = this.problemWithSolution.problem.codeExpectationItems.enableTestsRun;

            if (this.problemWithSolution.codeSolution.submissionId) {
                this.workbookService.getTestsResult(this.workbookId, this.assignmentIndex, true)
                    .subscribe(result => {
                        if (result) {
                            this.submissionResult = result;
                            this.submissionResult.status = (<any>CodeSubmissionStatus)[result.status];
                        }
                    }, (err) => {console.log("123", err)});
            }
        }

        const solution = this.problemWithSolution.solution;
        let i = 0;
        this.problemWithSolution.problem.options.forEach((o, i) => {
            let b = false;
            if (solution) {
                b = solution.charAt(i) !== '0';
            }
            this.problemWithSolution.problem.options[i].checked = b;
            i++;
        });
        this.oldSolution = this.stringifySolution();
    }

    private resetFlags(): void {
        this.problemWithSolution.problem.options.map(o => {
            o.checked = false;
            return o;
        });
    }

    typeByExpectation() {
        return this.problemWithSolution.problem.expectation === 'SINGLE' ? 'radio' : 'checkbox';
    }

    private stringifySolution(): string {
        const expectation = this.problemWithSolution.problem.expectation;
        if (expectation === 'SINGLE' || expectation === 'MULTIPLE') {
            return this.stringifyFlags();
        } else {
            return this.problemWithSolution.solution;
        }
    }

    isSolutionCorrect(): boolean {
        return this.problemWithSolution.score &&
            this.problemWithSolution.score === this.problemWithSolution.scoreMax;
    }

    private stringifyFlags(): string {
        const r: number[] = [];
        this.problemWithSolution.problem.options.forEach((b) => r.push(b.checked ? 1 : 0));
        return r.join('');
    }

    compileCode() {
        this.workbookService.updateCodeSolution(this.workbookId, this.assignmentIndex,
            this.problemWithSolution.codeSolution.code, this.problemWithSolution.codeSolution.language).subscribe(() => {
                this.workbookStateService.notifyCodeSolutionUpdated(this.workbookId, this.assignmentIndex,
                    this.problemWithSolution.codeSolution.code, this.problemWithSolution.codeSolution.language);
                this.workbookService.runTests(this.workbookId,
                    this.assignmentIndex)
                    .subscribe(result => {
                        this.testRunning = true;
                        this.submissionResult = result;
                        this.submissionResult.status = (<any>CodeSubmissionStatus)[result.status];
                        this.submissionTimer = setInterval(() => this.getSubmissionResult(), 3000);
                    });
                })
    }

    backToLastSuccessful() {
        this.workbookService.getTestsResult(this.workbookId, this.assignmentIndex, false, true).subscribe(result => {
            console.log("back to", result);
            this.problemWithSolution.codeSolution.code = result.sourceCode;
            this.problemWithSolution.codeSolution.language = result.language;
            this.workbookService.updateCodeSolution(this.workbookId, this.assignmentIndex, result.sourceCode, result.language)
                .subscribe(() => {
                    this.compileCode();
                });
        })
    }

    getSubmissionResult() {
        this.workbookService.getTestsResult(this.workbookId, this.assignmentIndex, true)
            .subscribe(result => {
                console.log("compile result", result);
                this.submissionResult = result;
                this.submissionResult.status = (<any>CodeSubmissionStatus)[result.status];
                if (!this.isWaitingStatus(this.submissionResult.status)) {
                    this.testRunning = false;
                    clearTimeout(this.submissionTimer);
                }
                if (this.isOkStatus(this.submissionResult.status)) {
                    this.problemWithSolution.codeSolution.lastSuccessfulSubmissionId = 'true';
                }
            })
    }

    isWaitingStatus(status: CodeSubmissionStatus) {
        return status === CodeSubmissionStatus.COMPILING ||
            status === CodeSubmissionStatus.WAITING_IN_QUEUE ||
            status === CodeSubmissionStatus.RUNNING_TEST;
    }

    isRunningTestStatus(status: CodeSubmissionStatus) {
        return status === CodeSubmissionStatus.RUNNING_TEST;
    }

    isWrongAnswer(status: CodeSubmissionStatus) {
        return status === CodeSubmissionStatus.WRONG_ANSWER;
    }

    isErrorStatus(status: CodeSubmissionStatus) {
        return !this.isWaitingStatus(status) && !this.isOkStatus(status);
    }

    isOkStatus(status: CodeSubmissionStatus) {
        return status === CodeSubmissionStatus.OK;
    }

    testNumberFormatting(testNumber: number) {
        return " № " + testNumber;
    }

    showBackToSuccessfulBtn() {
        return this.problemWithSolution.codeSolution.lastSuccessfulSubmissionId &&
            this.submissionResult && !this.isOkStatus(this.submissionResult.status)
            && !this.isWaitingStatus(this.submissionResult.status) && this.classified;
    }

    memoryFormatter(bytes: number) {
        const kb = bytes / 1024;
        const mb = kb / 1024;
        if (mb > 1) {
            return mb.toFixed(2) + ' МБ';
        }
        if (kb > 1) {
            return kb.toFixed(2) + ' КБ';
        }


        return bytes + ' Б';
    }

    timeFormatting(timeList: number[]) {
        const maxTime = Math.max.apply(null, timeList);
        return maxTime + ' мс';
    }

    decode(str: string) {
        return _.unescape(str);
    }

    onEditorFocusChange() {
        this.workbookService.updateCodeSolution(this.workbookId, this.assignmentIndex,
            this.problemWithSolution.codeSolution.code, this.problemWithSolution.codeSolution.language).subscribe(() => {
                this.workbookStateService.notifyCodeSolutionUpdated(this.workbookId, this.assignmentIndex,
                    this.problemWithSolution.codeSolution.code, this.problemWithSolution.codeSolution.language);
        });
    }

}
