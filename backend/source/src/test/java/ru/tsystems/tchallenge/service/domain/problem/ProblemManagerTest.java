package ru.tsystems.tchallenge.service.domain.problem;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import ru.tsystems.tchallenge.service.domain.problem.option.ProblemOptionDocument;
import ru.tsystems.tchallenge.service.domain.problem.option.ProblemOptionInvoice;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ru.tsystems.tchallenge.service.domain.problem.ProblemCategory.OOD;

@RunWith(SpringRunner.class)
public class ProblemManagerTest {
    @Mock
    ProblemRepository problemRepository;
    @Mock
    ProblemConverter problemConverter;
    @Mock
    MongoTemplate mongoTemplate;
    @InjectMocks
    ProblemManager problemManager;
    private ProblemDocument problemDocument;
    private ProblemInvoice problemInvoice;
    private static final String PROBLEM_ID = UUID.randomUUID().toString();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        problemDocument = ProblemDocument.builder()
                .caption("Caption")
                .question("question")
                .categories(Collections.singletonList(OOD))
                .difficulty(ProblemDifficulty.EASY)
                .expectation(ProblemExpectation.TEXT)
                .options(Collections.singletonList(ProblemOptionDocument
                        .builder()
                        .content("answer")
                        .correct(true)
                        .build()))
                .build();
        problemInvoice = new ProblemInvoice();
        problemInvoice.setCaption("Caption");
        problemInvoice.setQuestion("question");
        problemInvoice.setCategories(Collections.singletonList(OOD));
        problemInvoice.setDifficulty(ProblemDifficulty.EASY);
        problemInvoice.setExpectation(ProblemExpectation.TEXT);
        problemInvoice.setOptions(Collections.singletonList(ProblemOptionInvoice
                .builder()
                .content("answer")
                .correct(true)
                .build()));
    }

    @Test
    public void cannotUpdateDeletedProblems() {
        problemDocument.setStatus(ProblemStatus.DELETED);
        when(problemRepository.findById(any())).thenReturn(Optional.of(problemDocument));
        expectedException.expect(OperationException.class);
        expectedException.expectMessage("ERR_FORBIDDEN: Access denied");
        problemManager.update(PROBLEM_ID, problemInvoice);
        verifyNoMoreInteractions(problemRepository);
    }

    @Test
    public void updateApprovedProblem() {
        problemDocument.setStatus(ProblemStatus.APPROVED);
        problemDocument.setQuestion("1");
        problemInvoice.setStatus(ProblemStatus.APPROVED);
        problemInvoice.setQuestion("2");
        when(problemRepository.findById(any())).thenReturn(Optional.of(problemDocument));
        problemManager.update(PROBLEM_ID, problemInvoice);
        ArgumentCaptor<ProblemDocument> captor = ArgumentCaptor.forClass(ProblemDocument.class);
        verify(problemRepository).save(captor.capture());
        assertEquals(captor.getValue().getStatus(), ProblemStatus.MODIFIED);
    }
}
