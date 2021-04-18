package ru.tsystems.tchallenge.service.domain.problem;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import ru.tsystems.tchallenge.service.domain.problem.codeexpectationitems.ProblemCodeExpectationItemsDocument;
import ru.tsystems.tchallenge.service.domain.problem.image.ProblemImageDocument;
import ru.tsystems.tchallenge.service.domain.problem.option.ProblemOptionDocument;
import ru.tsystems.tchallenge.service.domain.problem.snippet.ProblemSnippetDocument;
import ru.tsystems.tchallenge.service.utility.data.AbstractDocument;

import java.util.List;
import java.util.Set;

@Document(collection = "problems")
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public final class ProblemDocument extends AbstractDocument {
    private List<ProblemCategory> categories;
    private Integer complexity;
    private ProblemDifficulty difficulty;
    private ProblemExpectation expectation;
    private List<ProblemImageDocument> images;
    private String introduction;
    private List<ProblemOptionDocument> options;
    private ProblemCodeExpectationItemsDocument codeExpectationItems;
    private String question;
    private String caption;
    private List<ProblemSnippetDocument> snippets;
    private ProblemStatus status;
    private Set<String> tagIds;
}
