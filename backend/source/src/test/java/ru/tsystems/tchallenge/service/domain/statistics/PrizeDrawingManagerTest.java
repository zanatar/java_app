package ru.tsystems.tchallenge.service.domain.statistics;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import ru.tsystems.tchallenge.service.domain.account.AccountConverter;
import ru.tsystems.tchallenge.service.domain.account.AccountDocument;
import ru.tsystems.tchallenge.service.domain.account.AccountRepository;
import ru.tsystems.tchallenge.service.domain.workbook.WorkbookDocument;
import ru.tsystems.tchallenge.service.domain.workbook.WorkbookRepository;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class PrizeDrawingManagerTest {


    @Mock
    private WorkbookRepository workbookRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountConverter accountConverter;

    @InjectMocks
    private PrizeDrawingManager prizeDrawingManager;


    private final Integer applicantsNumber = 5;

    private PrizeDrawingInvoice invoice;


    @Before
    public void setUp() {
        invoice = new PrizeDrawingInvoice();
        invoice.setEventId(UUID.randomUUID().toString());
        invoice.setThreshold(BigDecimal.valueOf(0.5));
        invoice.setMethod("random");

        Set<WorkbookDocument> workbooks = new HashSet<>();

        for (int i = 0; i < applicantsNumber; i++) {
            String ownerId = UUID.randomUUID().toString();
            WorkbookDocument document = Mockito.mock(WorkbookDocument.class);
            when(document.getOwnerId()).thenReturn(ownerId);

            AccountDocument account = Mockito.mock(AccountDocument.class);
            when(account.getId()).thenReturn(ownerId);

            when(accountRepository.findById(ownerId))
                    .thenReturn(Optional.of(account));
            workbooks.add(document);
        }

        when(workbookRepository.findPrizeApplicants(any(), any(), any()))
                .thenReturn(workbooks);

        when(accountConverter.toPersonality(any())).thenReturn(null);
    }

    @Test
    public void numberOfApplicants() {
        Assert.assertEquals(applicantsNumber, prizeDrawingManager.numberOfApplicants(invoice));

    }

    @Test
    public void prizeWinner() {
        /*
         * Positive test
         * Checks that one participant can be winner only once
         */
        Set<String> winners = new HashSet<>();
        invoice.setExcludingOwnerIds(new ArrayList<>());

        for (int i = 0; i < applicantsNumber; i++) {
            PrizeWinner prizeWinner = prizeDrawingManager.prizeWinner(invoice);
            Assert.assertFalse(winners.contains(prizeWinner.getId()));
            invoice.getExcludingOwnerIds().add(prizeWinner.getId());
            winners.add(prizeWinner.getId());
        }
    }


    @Test(expected = OperationException.class)
    public void prizeWinnerNegative() {
        /*
         * Negative test
         * Checks that if all participants
         * (satisfying criteria, which is not part of this test)
         * are winners, next call of this function will throw exception
         */
        invoice.setExcludingOwnerIds(new ArrayList<>());

        for (int i = 0; i < applicantsNumber; i++) {
            PrizeWinner prizeWinner = prizeDrawingManager.prizeWinner(invoice);
            invoice.getExcludingOwnerIds().add(prizeWinner.getId());
        }

        prizeDrawingManager.prizeWinner(invoice);
    }

}
