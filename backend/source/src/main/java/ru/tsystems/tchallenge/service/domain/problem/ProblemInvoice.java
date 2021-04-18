package ru.tsystems.tchallenge.service.domain.problem;

import com.google.common.base.Strings;
import lombok.Data;
import ru.tsystems.tchallenge.service.domain.problem.codeexpectationitems.ProblemCodeExpectationItemsInvoice;
import ru.tsystems.tchallenge.service.domain.problem.image.ProblemImage;
import ru.tsystems.tchallenge.service.domain.problem.option.ProblemOptionInvoice;
import ru.tsystems.tchallenge.service.domain.problem.snippet.ProblemSnippetInvoice;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder;
import ru.tsystems.tchallenge.service.utility.validation.ValidationAware;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder.missing;
import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.*;

@Data
public final class ProblemInvoice implements ValidationAware, Serializable {

    private List<ProblemCategory> categories;
    private Integer complexity;
    private ProblemDifficulty difficulty;
    private String caption;
    private ProblemExpectation expectation;
    private String introduction;
    private List<ProblemOptionInvoice> options;
    private ProblemCodeExpectationItemsInvoice codeExpectationItems;
    private String question;
    private List<ProblemSnippetInvoice> snippets;
    private List<ProblemImage> images;
    private ProblemStatus status;
    private Set<String> tagIds;

    @Override
    public void registerViolations() {
        if (categories == null || categories.isEmpty()) {
            throw missingProblemCategories();
        }

        if (Strings.isNullOrEmpty(caption)) {
            throw captionIsMissing();
        }

        if (expectation == null) {
            throw expectationIsMissing();
        }

        if (expectation == ProblemExpectation.CODE) {
            if (codeExpectationItems.getContest().getTests() == null) {
                throw missing(ERR_CONTEST, "tests");
            }
        } else {

            if (options == null || options.isEmpty()) {
                throw optionsAreRequired();
            }

            options.forEach(ValidationAware::validate);

            if (options.stream().noneMatch(ProblemOptionInvoice::getCorrect)) {
                throw noCorrectAnswer();
            }
        }

        if (expectation == ProblemExpectation.TEXT && options.size() != 1) {
            throw invalidTextOption();
        }

        if (Strings.isNullOrEmpty(question)) {
            throw questionIsMissing();
        }
    }

    private OperationException missingProblemCategories() {
        return OperationExceptionBuilder.operationException()
                .description("Problem categories missing")
                .textcode(ERR_PROBLEM_CATEGORY)
                .build();
    }

    private OperationException captionIsMissing() {
        return OperationExceptionBuilder.operationException()
                .description("Caption is required")
                .textcode(ERR_CAPTION)
                .build();
    }

    private OperationException expectationIsMissing() {
        return OperationExceptionBuilder.operationException()
                .description("Expectation is missing")
                .textcode(ERR_PROBLEM_EXPECTATION)
                .build();
    }

    private OperationException optionsAreRequired() {
        return OperationExceptionBuilder.operationException()
                .description("Options are required")
                .textcode(ERR_PROBLEM_OPTION)
                .build();
    }

    private OperationException noCorrectAnswer() {
        return OperationExceptionBuilder.operationException()
                .description("Problem doesn't contain correct option")
                .textcode(ERR_PROBLEM_NO_CORRECT_OPTION)
                .build();
    }


    private OperationException questionIsMissing() {
        return OperationExceptionBuilder.operationException()
                .description("Question is missing")
                .textcode(ERR_PROBLEM_QUESTION)
                .build();
    }

    private OperationException invalidTextOption() {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_PROBLEM_TEXT_OPTION)
                .description("Problem with text options expectation should have exactly " +
                        "1 correct option")
                .build();
    }
}
