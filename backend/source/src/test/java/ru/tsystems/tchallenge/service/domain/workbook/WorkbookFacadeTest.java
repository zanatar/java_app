package ru.tsystems.tchallenge.service.domain.workbook;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import ru.tsystems.tchallenge.service.domain.account.AccountCategory;
import ru.tsystems.tchallenge.service.domain.account.AccountRole;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.security.authentication.UserAuthentication;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
@RunWith(SpringRunner.class)
public class WorkbookFacadeTest {
    @Mock
    private WorkbookDocument workbookDocument;
    @Mock
    private MongoTemplate mongoTemplate;
    @Mock
    private WorkbookRepository workbookRepository;
    @Mock
    private WorkbookManager workbookManager;
    @InjectMocks
    private WorkbookFacade workbookFacade;

    private static final String WORKBOOK_ID = UUID.randomUUID().toString();
    private static final String ERR_FORBIDDEN_ACCESS_DENIED = "ERR_FORBIDDEN: Access denied";
    @Rule
    public ExpectedException expectedException = ExpectedException.none();


    @Test
    public void participantCannotRetrieveByIdOthersWorkbook() {
        UserAuthentication authentication = UserAuthentication.builder()
                .accountId("id1")
                .accountCategory(AccountCategory.PARTICIPANT)
                .authorities(Collections.singleton(AccountRole.PARTICIPANT))
                .build();
        when(workbookDocument.getOwnerId()).thenReturn("id2");
        when(workbookRepository.findById(any())).thenReturn(Optional.of(workbookDocument));
        expectedException.expect(OperationException.class);
        expectedException.expectMessage(ERR_FORBIDDEN_ACCESS_DENIED);
        workbookFacade.retrieveById(authentication, WORKBOOK_ID);
    }

    @Test
    public void participantCannotCheckIfOthersWorkbookIsReviewed() {
        UserAuthentication authentication = UserAuthentication.builder()
                .accountId("id1")
                .accountCategory(AccountCategory.PARTICIPANT)
                .authorities(Collections.singleton(AccountRole.PARTICIPANT))
                .build();
        when(workbookDocument.getOwnerId()).thenReturn("id2");
        when(workbookRepository.findById(any())).thenReturn(Optional.of(workbookDocument));
        expectedException.expect(OperationException.class);
        expectedException.expectMessage(ERR_FORBIDDEN_ACCESS_DENIED);
        workbookFacade.isReviewed(authentication, WORKBOOK_ID);
    }
}
