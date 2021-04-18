package ru.tsystems.tchallenge.service.domain.problem;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import ru.tsystems.tchallenge.service.domain.problem.codeexpectationitems.ProblemCodeExpectationItemsDocument;
import ru.tsystems.tchallenge.service.domain.problem.codeexpectationitems.contest.ProblemContestDocument;
import ru.tsystems.tchallenge.service.domain.problem.codeexpectationitems.contest.test.ProblemTest;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;


@RunWith(SpringRunner.class)
public class ProblemManagerTest2 {
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
   /*     problemDocument = ProblemDocument.builder()
                .caption("Caption")
                .question("question")
                .categories( new ArrayList() {{
                    add(ProblemCategory.OOD);
                    add(ProblemCategory.JAVA);
                }} )
                .difficulty(ProblemDifficulty.ULTIMATE)
                .expectation(ProblemExpectation.CODE)
                .problemCodeExpectationItems(
                        ProblemCodeExpectationItemsDocument.builder()
                            .predefinedCode("public class Main{ public static void main(String args[]){ System.out.print('ku-ku'); }}")
                            .predefinedLang("java")
                            .contest(
                                    ProblemContestDocument.builder()
                                        .tests(
                                                new ArrayList<ProblemTest>()
                                                {{
                                                    add( new ProblemTest("dfgdgd", "sdfsdfsf") );
                                                    add( new ProblemTest("11121xcxscxc", "2xc xcx222") );
                                                }}
                                        )
                                        .timeLimit(20000)
                                        .memoryLimit(4000)
                                        .build()
                            )
                            .enableTestsRun(true)
                            .build()
                )
                .build();*/
      /*  problemInvoice = new ProblemInvoice();
        problemInvoice.setCaption("Caption");
        problemInvoice.setQuestion("question");
        problemInvoice.setCategories(Collections.singletonList(OOD));
        problemInvoice.setDifficulty(ProblemDifficulty.EASY);
        problemInvoice.setExpectation(ProblemExpectation.TEXT);
        problemInvoice.setOptions(Collections.singletonList(ProblemOptionInvoice
                .builder()
                .content("answer")
                .correct(true)
                .build()));*/
    }

    @Test
    public void showProblem() {
      /*  problemDocument.setStatus(ProblemStatus.APPROVED);
        problemDocument.setQuestion("1");
        problemInvoice.setStatus(ProblemStatus.APPROVED);
        problemInvoice.setQuestion("2");
        when(problemRepository.findById(any())).thenReturn(Optional.of(problemDocument));
        problemManager.update(PROBLEM_ID, problemInvoice);
        ArgumentCaptor<ProblemDocument> captor = ArgumentCaptor.forClass(ProblemDocument.class);
        verify(problemRepository).save(captor.capture());*/

        //assertEquals(captor.getValue().getStatus(), ProblemStatus.MODIFIED);

      //  System.out.println(problemDocument);
    }
}
